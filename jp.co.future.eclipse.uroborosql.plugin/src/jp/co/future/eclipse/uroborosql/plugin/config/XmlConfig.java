package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.config.Internal.ClassesData;
import jp.co.future.eclipse.uroborosql.plugin.config.Internal.LabelMetadata;
import jp.co.future.eclipse.uroborosql.plugin.config.Internal.PackagesData;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.AttributableValue;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.ITarget;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.Xml;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Column;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Columns;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Table;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Tables;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Const;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Variables;
import jp.co.future.eclipse.uroborosql.plugin.utils.CacheContainer;
import jp.co.future.eclipse.uroborosql.plugin.utils.CacheContainer.CacheContainerMap;
import jp.co.future.eclipse.uroborosql.plugin.utils.CacheContainer.CachePredicate;

public class XmlConfig implements PluginConfig {

	private enum ConstCacheKey implements CachePredicate<Variables, RuntimeException> {
		CONST_JAVACLASS(TimeUnit.SECONDS.toMillis(5)), //
		CONST_DB(TimeUnit.SECONDS.toMillis(60)), //
		;
		private final long expiration;

		ConstCacheKey(long expiration) {
			this.expiration = expiration;
		}

		@Override
		public boolean test(Variables data, long time) {
			return time + expiration > System.currentTimeMillis();
		}
	}

	private final Document document;
	private final IProject project;

	private final CacheContainerMap<ConstCacheKey, Variables, RuntimeException> consts = CacheContainer
			.createMap(ConstCacheKey::test);

	private final CacheContainer<Tables, RuntimeException> tables = new CacheContainer<>(
			(data, time) -> time + TimeUnit.HOURS.toMillis(8) > System.currentTimeMillis());

	private final CacheContainerMap<String, Columns, RuntimeException> columns = CacheContainer.createMap(
			(data, time) -> time + TimeUnit.SECONDS.toMillis(60) > System.currentTimeMillis());

	public XmlConfig(byte[] input, IProject project)
			throws IOException, ParserConfigurationException, SAXException {
		this(new ByteArrayInputStream(input), project);
	}

	public XmlConfig(InputStream input, IProject project)
			throws IOException, ParserConfigurationException, SAXException {
		document = Xml.parse(input);
		this.project = project;
	}

	@Override
	public String getSqlId() {
		return getTarget("sqlid").value().orElseGet(PluginConfig.super::getSqlId);
	}

	@Override
	public Variables getConsts() {
		Variables constVariables = new Variables();

		try {
			constVariables.putAll(consts.get(ConstCacheKey.CONST_JAVACLASS).orElseGet(() -> loadConstsFromClasses()));
		} catch (RuntimeException e) {
		}
		try {
			constVariables.putAll(consts.get(ConstCacheKey.CONST_DB).orElseGet(() -> loadConstsFromDb()));
		} catch (RuntimeException e) {
		}

		return constVariables;
	}

	@Override
	public Tables getTables(String text, boolean lazy) {
		if (!testTableLength(text)) {
			return new Tables();
		}

		Tables tables = this.tables.orElseGet(Tables::new);

		if (!lazy) {
			tables.addAll(findTablesFromDb(text));
		} else {
			tables.addAll(findTablesFromDb(text));
			tables.addAll(lazyfindTablesFromDb(text));
		}
		return tables;
	}

	private boolean testTableLength(String text) {
		Optional<String> optMin = getTarget("contentassist", "tables", "minlength").value();
		if (optMin.isPresent()) {
			int minlength;
			try {
				minlength = Integer.parseInt(optMin.get());
				if (text.length() < minlength) {
					return false;
				}
			} catch (NumberFormatException e) {
			}
		}
		optMin = getTarget("contentassist", "tables", "minbytes").value();
		if (optMin.isPresent()) {
			int minbytes;
			try {
				minbytes = Integer.parseInt(optMin.get());
				if (text.getBytes(StandardCharsets.UTF_8).length < minbytes) {
					return false;
				}
			} catch (NumberFormatException e) {
			}
		}
		return text.length() >= 3 || text.getBytes(StandardCharsets.UTF_8).length >= 6;
	}

	@Override
	public Columns getColumn(Table table) {
		try {
			return columns.get(table.getName()).orElseGet(() -> {
				List<AttributableValue> selects = getTarget("contentassist", "columns", "sql").list();
				if (selects.isEmpty()) {
					return new Columns();
				}
				Map<String, Map<Integer, String>> dbMap = new HashMap<>();
				for (int i = 0; i < selects.size(); i++) {
					AttributableValue select = selects.get(i);
					String db = select.attr("db").orElse("");
					dbMap.computeIfAbsent(db, k -> new HashMap<>()).put(i, select.value());
				}

				Columns result = new Columns();
				dbMap.forEach((db, sels) -> {
					DbInfo dbInfo = getDbInfo(db);
					Internal.connect(project, dbInfo, conn -> {
						for (Map.Entry<Integer, String> e : sels.entrySet()) {
							result.addAll(loadColumns(conn, e.getValue(), table, e.getKey()));
						}
						return null;
					});
				});
				return result;
			});
		} catch (RuntimeException e) {
			return new Columns();
		}
	}

	@Override
	public DbInfos getDbInfos() {
		return new DbInfos() {

			@Override
			public DbInfo get() {
				String url = getTarget("db", "url").value().map(v -> v.trim()).orElse(null);
				String user = getTarget("db", "user").value().map(v -> v.trim()).orElse(null);
				String password = getTarget("db", "password").value().map(v -> v.trim()).orElse(null);
				String driver = getTarget("db", "driver").value().map(v -> v.trim()).orElse(null);
				List<String> classpaths = getTarget("db", "classpath").values().stream().map(v -> v.trim())
						.collect(Collectors.toList());
				return new DbInfo(url, user, password, driver, classpaths);
			}

			@Override
			public DbInfo get(String name) {
				List<AttributableValue> dbs = getTarget("db").list();
				for (AttributableValue db : dbs) {
					if (!db.element().isPresent()) {
						continue;
					}
					Element elm = db.element().get();
					if (db.attr("name").orElse("").equals(name)) {
						String url = ITarget.get(elm, "url").value().map(v -> v.trim()).orElse(null);
						String user = ITarget.get(elm, "user").value().map(v -> v.trim()).orElse(null);
						String password = ITarget.get(elm, "password").value().map(v -> v.trim()).orElse(null);
						String driver = ITarget.get(elm, "driver").value().map(v -> v.trim()).orElse(null);
						List<String> classpaths = ITarget.get(elm, "classpath").values().stream().map(v -> v.trim())
								.collect(Collectors.toList());
						return new DbInfo(url, user, password, driver, classpaths);
					}
				}
				return null;
			}
		};
	}

	public int sql(String sql) {
		DbInfo dbInfo = getDbInfo("");
		return Internal.connect(project, dbInfo, conn -> {
			conn.setAutoCommit(true);
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				return ps.executeUpdate();
			}
		}).orElse(0);
	}

	/**
	 * class から定数値の読み込み
	 * @return
	 */
	private Variables loadConstsFromClasses() {

		Variables consts = new Variables();
		String constParamPrefix = getTarget("sqlContextFactory", "constParamPrefix").value().orElse("CLS_");
		List<String> constantClassNames = getTarget("sqlContextFactory", "constantClassName")
				.values().stream()
				.map(String::trim).collect(Collectors.toList());
		List<String> enumConstantPackageNames = getTarget("sqlContextFactory", "enumConstantPackageName")
				.values().stream()
				.map(String::trim).collect(Collectors.toList());

		ClassesData classesData = Internal.getClassesData(project, constantClassNames);
		PackagesData packagesData = Internal.getPackagesData(project, enumConstantPackageNames);

		SqlContextFactoryImpl contextFactory = new SqlContextFactoryImpl();
		consts.putAll(contextFactory.buildConstParamMap(constParamPrefix, classesData));
		consts.putAll(contextFactory.buildEnumConstParamMap(constParamPrefix, packagesData));
		return consts;
	}

	private Variables loadConstsFromDb() {
		List<AttributableValue> selects = getTarget("sqlContextFactory", "constantSql").list();
		if (selects.isEmpty()) {
			return new Variables();
		}

		Map<String, Set<String>> dbMap = new HashMap<>();
		for (AttributableValue select : selects) {
			String db = select.attr("db").orElse("");
			dbMap.computeIfAbsent(db, k -> new HashSet<>()).add(select.value());
		}

		Variables variables = new Variables();
		dbMap.forEach((db, sels) -> {
			DbInfo dbInfo = getDbInfo(db);
			Internal.connect(project, dbInfo, conn -> {
				for (String sel : sels) {
					variables.putAll(loadDbConst(conn, sel));
				}
				return null;
			});
		});

		return variables;

	}

	private Collection<Table> findTablesFromDb(String text) {
		List<AttributableValue> selects = getTarget("contentassist", "tables", "sql").list();
		if (selects.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, Map<Integer, String>> dbMap = new HashMap<>();
		for (int i = 0; i < selects.size(); i++) {
			AttributableValue select = selects.get(i);
			String db = select.attr("db").orElse("");
			dbMap.computeIfAbsent(db, k -> new HashMap<>()).put(i, select.value());
		}

		Set<Table> result = new HashSet<>();
		dbMap.forEach((db, sels) -> {
			DbInfo dbInfo = getDbInfo(db);
			Internal.connect(project, dbInfo, conn -> {
				for (Map.Entry<Integer, String> e : sels.entrySet()) {
					result.addAll(loadTables(conn, e.getValue(), text, e.getKey()));
				}
				return null;
			});
		});
		return result;
	}

	private Collection<Table> lazyfindTablesFromDb(String text) {
		List<AttributableValue> selects = getTarget("contentassist", "tables", "lazySql").list();
		if (selects.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, Map<Integer, String>> dbMap = new HashMap<>();
		for (int i = 0; i < selects.size(); i++) {
			AttributableValue select = selects.get(i);
			String db = select.attr("db").orElse("");
			dbMap.computeIfAbsent(db, k -> new HashMap<>()).put(i, select.value());
		}

		Set<Table> result = new HashSet<>();
		dbMap.forEach((db, sels) -> {
			DbInfo dbInfo = getDbInfo(db);
			Internal.connect(project, dbInfo, conn -> {
				for (Map.Entry<Integer, String> e : sels.entrySet()) {
					result.addAll(loadTables(conn, e.getValue(), text, e.getKey()));
				}
				return null;
			});
		});
		return result;
	}

	private Variables loadDbConst(Connection conn, String select) throws SQLException {
		return Internal.query(project, conn, select, Collections.emptyMap(), (rs) -> {
			Variables variables = new Variables();
			LabelMetadata[] labels = Internal.getLabelMetadatas(rs, new String[][] {
					{ "name", "key" },
					{ "value" }
			});
			while (rs.next()) {
				String name = labels[0].getString(rs);
				Object value = labels[1].getObject(rs);
				String description = labels[2].getString(rs);
				variables.put(new Const(name, () -> value, () -> description));
			}
			return variables;
		});
	}

	private Collection<Table> loadTables(Connection conn, String select, String text, int priority)
			throws SQLException {
		return Internal.query(project, conn, select, Table.getNameParamMap(text), (rs) -> {
			List<Table> result = new ArrayList<>();
			LabelMetadata[] labels = Internal.getLabelMetadatas(rs, new String[][] {
					generateIdentifierNames("table", "table", "name,nm"),
					generateIdentifierNames("comment,comments,description", "table", "comment,comments,description"),
			});
			while (rs.next()) {
				String name = labels[0].getString(rs);
				String comment = labels[1].getString(rs);
				String description = labels[2].getString(rs);
				result.add(new Table(this, name, comment, description, priority));
			}
			return result;
		});
	}

	private List<Column> loadColumns(Connection conn, String select, Table table, int priority) throws SQLException {
		return Internal.query(project, conn, select, table.getNameParamMap(), (rs) -> {
			List<Column> result = new ArrayList<>();
			LabelMetadata[] labels = Internal.getLabelMetadatas(rs, new String[][] {
					generateIdentifierNames("column,col", "column,col", "name,nm"),
					generateIdentifierNames("comment,comments,description", "column,col",
							"comment,comments,description"),
			});
			while (rs.next()) {
				String name = labels[0].getString(rs);
				String comment = labels[1].getString(rs);
				String description = labels[2].getString(rs);
				result.add(new Column(table, name, comment, description, priority));
			}
			return result;
		});
	}

	private String[] generateIdentifierNames(String name, String... names) {
		List<String> result = new ArrayList<>(Arrays.asList(name.split(",")));
		result.addAll(generateIdentifierNames(Arrays.asList(names)));
		return result.toArray(new String[result.size()]);
	}

	private List<String> generateIdentifierNames(List<String> names) {
		if (names.isEmpty()) {
			return Collections.emptyList();
		}

		LinkedList<String> namesList = new LinkedList<>(names);

		String fnames = namesList.poll();

		List<String> result = new ArrayList<>();
		for (String fname : fnames.split(",")) {
			generateIdentifierNames(namesList).forEach(n -> {
				result.add(fname + "_" + n);
			});
		}
		return result;
	}

	private ITarget getTarget(String... tags) {
		Element root = document.getDocumentElement();
		return ITarget.get(root, tags);
	}

}
