package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.config.Internal.ClassesData;
import jp.co.future.eclipse.uroborosql.plugin.config.Internal.PackagesData;
import jp.co.future.eclipse.uroborosql.plugin.config.xml.ITarget;

public class XmlConfig implements PluginConfig {
	private final Document document;
	private final IProject project;
	private Map<String, Object> consts;

	@Override
	public String getSqlId() {
		return getTarget("sqlid").value().orElseGet(PluginConfig.super::getSqlId);
	}

	@Override
	public Map<String, ?> getConsts() {
		if (consts == null) {
			consts = new HashMap<>();
			consts.putAll(loadConstsByClasses());
			consts.putAll(loadConstsByDb());
		}

		return consts;
	}

	/**
	 * class から定数値の読み込み
	 * @return
	 */
	private Map<String, Object> loadConstsByClasses() {

		Map<String, Object> consts = new HashMap<>();
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

	private Map<String, Object> loadConstsByDb() {
		String select = getTarget("sqlContextFactory", "constantSql")
				.value().map(v -> v.trim()).orElse(null);
		if (select == null) {
			return Collections.emptyMap();
		}

		String url = getTarget("db", "url").value().map(v -> v.trim()).orElse(null);
		String user = getTarget("db", "user").value().map(v -> v.trim()).orElse(null);
		String password = getTarget("db", "password").value().map(v -> v.trim()).orElse(null);
		String driver = getTarget("db", "driver").value().map(v -> v.trim()).orElse(null);

		return Internal.connect(project, driver, url, user, password, conn -> {
			Map<String, Object> map = new HashMap<>();
			try (PreparedStatement ps = conn.prepareStatement(select);
					ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					String name = rs.getString(1);
					Object value = null;
					try {
						value = rs.getString(2);
					} catch (SQLException e) {
					}
					map.put(name, value);
				}
			}
			return map;

		}).orElseGet(Collections::emptyMap);
	}

	public XmlConfig(Path path, IProject project) throws IOException, ParserConfigurationException, SAXException {
		try (InputStream input = Files.newInputStream(path)) {
			DocumentBuilder builder = newDocumentBuilder();
			document = builder.parse(input);
		}
		this.project = project;
	}

	private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);//dtd読み込みをしない
		return builderFactory.newDocumentBuilder();
	}

	private ITarget getTarget(String... tags) {
		Element root = document.getDocumentElement();
		return ITarget.get(root, tags);
	}

}
