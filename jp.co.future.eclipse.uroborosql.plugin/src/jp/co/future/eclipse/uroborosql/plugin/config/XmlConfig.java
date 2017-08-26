package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.config.Internal.ClassesData;
import jp.co.future.eclipse.uroborosql.plugin.config.Internal.PackagesData;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.ITarget;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.Xml;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.Const;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.Variables;

public class XmlConfig implements PluginConfig {
	private final Document document;
	private final IProject project;
	private Variables consts;

	@Override
	public String getSqlId() {
		return getTarget("sqlid").value().orElseGet(PluginConfig.super::getSqlId);
	}

	@Override
	public Variables getConsts() {
		if (consts == null) {
			consts = new Variables();
			consts.putAll(loadConstsByClasses());
			consts.putAll(loadConstsByDb());
		}

		return consts;
	}

	/**
	 * class から定数値の読み込み
	 * @return
	 */
	private Variables loadConstsByClasses() {

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

	private Variables loadConstsByDb() {
		List<String> selects = getTarget("sqlContextFactory", "constantSql")
				.values().stream().map(v -> v.trim())
				.collect(Collectors.toList());
		if (selects.isEmpty()) {
			return new Variables();
		}

		String url = getTarget("db", "url").value().map(v -> v.trim()).orElse(null);
		String user = getTarget("db", "user").value().map(v -> v.trim()).orElse(null);
		String password = getTarget("db", "password").value().map(v -> v.trim()).orElse(null);
		String driver = getTarget("db", "driver").value().map(v -> v.trim()).orElse(null);
		List<String> classpaths = getTarget("db", "classpath").values().stream().map(v -> v.trim())
				.collect(Collectors.toList());

		return Internal.connect(project, driver, url, user, password, classpaths, conn -> {
			Variables variables = new Variables();
			for (String select : selects) {
				variables.putAll(loadDbConst(conn, select));
			}
			return variables;

		}).orElseGet(Variables::new);
	}

	private Variables loadDbConst(Connection conn, String select) throws SQLException {
		Variables variables = new Variables();
		try (PreparedStatement ps = conn.prepareStatement(select);
				ResultSet rs = ps.executeQuery();) {
			while (rs.next()) {
				String name = rs.getString(1);
				Object value = null;
				try {
					value = rs.getObject(2);
				} catch (SQLException e) {
					variables.put(new Const(name));
					continue;
				}
				String description = null;
				try {
					description = rs.getString(3);
				} catch (SQLException e) {
					variables.put(new Const(name, value));
					continue;
				}
				variables.put(new Const(name, value, description));
			}
		}
		return variables;
	}

	public XmlConfig(byte[] input, IProject project)
			throws IOException, ParserConfigurationException, SAXException {
		this(new ByteArrayInputStream(input), project);
	}

	public XmlConfig(InputStream input, IProject project)
			throws IOException, ParserConfigurationException, SAXException {
		document = Xml.parse(input);
		this.project = project;
	}

	private ITarget getTarget(String... tags) {
		Element root = document.getDocumentElement();
		return ITarget.get(root, tags);
	}

}
