package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig.DbInfo;
import jp.co.future.eclipse.uroborosql.plugin.config.executor.Executor;
import jp.co.future.eclipse.uroborosql.plugin.config.executor.JdbcExecutor;
import jp.co.future.eclipse.uroborosql.plugin.config.executor.UroboroSQLExecutor;
import jp.co.future.eclipse.uroborosql.plugin.config.utils.SQLFunction;
import jp.co.future.eclipse.uroborosql.plugin.utils.CacheContainer;
import jp.co.future.eclipse.uroborosql.plugin.utils.CacheContainer.CacheContainerMap;

class Internal {
	static abstract class AbsJavaData {
		private final Collection<URL> urls;

		AbsJavaData(Collection<URL> urls) {
			this.urls = urls;
		}

		public ClassLoader createURLClassLoader() {
			return Internal.createCustomClassLoader(urls);
		}

	}

	static class ClassesData extends AbsJavaData {

		private final Collection<String> loaderTargetClassNames;
		private final Collection<IType> sourceTypes;

		public ClassesData(Collection<URL> urls, Collection<String> loaderTargetClassNames,
				Collection<IType> sourceTypes) {
			super(urls);
			this.loaderTargetClassNames = loaderTargetClassNames;
			this.sourceTypes = sourceTypes;
		}

		public Collection<IType> getSourceTypes() {
			return sourceTypes;
		}

		public Collection<String> getLoaderTargetClassNames() {
			return loaderTargetClassNames;
		}
	}

	static class PackagesData extends AbsJavaData {
		private final Collection<String> loaderTargetPackageNames;
		private final Map<String, Collection<IType>> sourceTypes;

		public PackagesData(Collection<URL> urls, Collection<String> loaderTargetPackageNames,
				Map<String, Collection<IType>> sourceTypes) {
			super(urls);
			this.loaderTargetPackageNames = loaderTargetPackageNames;
			this.sourceTypes = sourceTypes;
		}

		public Map<String, Collection<IType>> getSourceTypes() {
			return sourceTypes;
		}

		public Collection<String> getLoaderTargetPackageNames() {
			return loaderTargetPackageNames;
		}

	}

	private static class CustomClassLoader extends URLClassLoader {
		private final Set<String> defined = new HashSet<>();
		private final ClassLoader defaultClassLoader;

		public CustomClassLoader(URL[] urls) {
			//			super(URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader()));
			super(urls);
			defaultClassLoader = Thread.currentThread().getContextClassLoader();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T> Class<T> defineAndLoadClass(Class<? extends T> type) throws ClassNotFoundException, IOException {
			String name = type.getName();
			if (defined.add(name)) {
				try (InputStream input = CustomClassLoader.class.getClassLoader()
						.getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
						ByteArrayOutputStream output = new ByteArrayOutputStream();) {
					copy(input, output);
					byte[] b = output.toByteArray();
					defineClass(name, b, 0, b.length);
				}
			}

			return (Class) loadClass(name);
		}

		private static long copy(InputStream source, OutputStream sink)
				throws IOException {
			long nread = 0L;
			byte[] buf = new byte[8192];
			int n;
			while ((n = source.read(buf)) > 0) {
				sink.write(buf, 0, n);
				nread += n;
			}
			return nread;
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			try {
				return super.loadClass(name, resolve);
			} catch (ClassNotFoundException e) {
				return defaultClassLoader.loadClass(name);
			}
		}

	}

	public static CustomClassLoader createCustomClassLoader(Collection<?> classpaths) {
		List<URL> urls = new ArrayList<>();
		for (Object object : classpaths) {
			if (object instanceof URL) {
				urls.add((URL) object);
			} else {
				String classpath = object.toString();
				try {
					urls.add(new URL(classpath));
					continue;
				} catch (MalformedURLException e) {
				}

				try {
					urls.add(Paths.get(classpath).toUri().toURL());
				} catch (MalformedURLException e) {
					// ignore
				}
			}
		}

		return new CustomClassLoader(urls.toArray(new URL[urls.size()]));
	}

	public static IProject getProject(IEditorPart editor) {
		IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		IFile file = editorInput.getFile();

		return file.getProject();
	}

	private static final CacheContainerMap<Path, PluginConfig, IOException> cachePluginConfig = CacheContainer
			.createMap((p, c, time) -> {
				FileTime fileTime = Files.getLastModifiedTime(p);
				if (fileTime != null) {
					return fileTime.toMillis() < time;
				}
				return false;
			});

	public static PluginConfig getConfig(IProject project) {
		try {
			Path location = Paths.get(project.getDescription().getLocationURI());
			Path path = location.resolve(".uroborosqlpluginrc.xml");
			if (Files.exists(path)) {
				return cachePluginConfig.get(path).orElseGet(() -> {
					try (InputStream input = Files.newInputStream(path)) {
						PluginConfig config = new XmlConfig(input, project);
						return config;
					}
				});

			}

			return null;
		} catch (IOException e) {
		} catch (Exception e) {
		}
		return DefaultConfig.getInstance();
	}

	public static ClassesData getClassesData(IProject project, List<String> classNames) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return new ClassesData(Collections.emptyList(), classNames, Collections.emptyList());
		}
		Set<URL> urls = new HashSet<>();
		Set<String> loaderTargetClassNames = new LinkedHashSet<>();
		Set<IType> sourceTypes = new LinkedHashSet<>();
		for (String className : classNames) {
			try {
				IType type = javaProject.findType(className);

				if (type == null) {
					continue;
				}
				IClassFile cf = type.getClassFile();
				if (cf != null) {
					try {
						URL url = cf.getPath().toFile().toURI().toURL();
						urls.add(url);
						loaderTargetClassNames.add(className);
						continue;
					} catch (MalformedURLException e) {
					}
				}
				sourceTypes.add(type);
			} catch (JavaModelException e) {
			}
		}
		return new ClassesData(urls, loaderTargetClassNames, sourceTypes);
	}

	public static PackagesData getPackagesData(IProject project, List<String> packageNames) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return new PackagesData(Collections.emptyList(), packageNames, Collections.emptyMap());
		}
		try {
			Set<URL> urls = new HashSet<>();
			Set<String> loaderTargetPackageNames = new LinkedHashSet<>();
			Map<String, Collection<IType>> sourceTypes = new HashMap<>();
			for (IJavaProject pj : getRequiredProjects(javaProject)) {
				try {
					for (IPackageFragment packageFragment : pj.getPackageFragments()) {
						Optional<String> targetPackage = getTargetPackage(packageFragment.getElementName(),
								packageNames);
						if (targetPackage.isPresent()) {
							try {
								for (IJavaElement element : packageFragment.getChildren()) {
									if (element instanceof IClassFile) {
										//								IType type = ((IClassFile)element).getType();
										try {
											URL url = ((IClassFile) element).getPath().toFile().toURI().toURL();
											urls.add(url);
											loaderTargetPackageNames.add(packageFragment.getElementName());
											continue;
										} catch (MalformedURLException e) {
											// ignore
										}
										sourceTypes.computeIfAbsent(targetPackage.get(), k -> new LinkedHashSet<>())
												.add(((IClassFile) element).getType());

									} else if (element instanceof ICompilationUnit) {
										try {
											for (IType type : ((ICompilationUnit) element).getAllTypes()) {
												sourceTypes.computeIfAbsent(targetPackage.get(),
														k -> new LinkedHashSet<>()).add(type);
											}
										} catch (JavaModelException e) {
										}
									}
								}
							} catch (JavaModelException e) {
							}

						}
					}
				} catch (JavaModelException e) {
				}
			}
			return new PackagesData(urls, loaderTargetPackageNames, sourceTypes);
		} catch (JavaModelException e) {
		}
		return new PackagesData(Collections.emptyList(), packageNames, Collections.emptyMap());
	}

	private static Collection<IJavaProject> getRequiredProjects(IJavaProject javaProject) throws JavaModelException {
		List<String> names = Arrays.asList(javaProject.getRequiredProjectNames());
		Set<IJavaProject> projects = new HashSet<>();
		projects.add(javaProject);
		for (IJavaProject pj : javaProject.getJavaModel().getJavaProjects()) {
			if (names.contains(pj.getElementName())) {
				projects.add(pj);
			}
		}
		return projects;
	}

	private static Optional<String> getTargetPackage(String elementName, List<String> packageNames) {
		return packageNames.stream()
				.filter(nm -> elementName.equals(nm) || (elementName + ".").startsWith(nm))
				.findFirst();
	}

	public static <R> Optional<R> connect(IProject project, DbInfo dbInfo, SQLFunction<Connection, R> f) {

		URL classUrl = getDriverUrlFromJavaProject(project, dbInfo.getDriver());
		List<Object> urls = new ArrayList<>();
		if (classUrl != null) {
			urls.add(classUrl);
		}
		urls.addAll(dbInfo.getClasspaths());
		if (urls.isEmpty()) {
			return Optional.empty();
		}
		try {
			ClassLoader classLoader = createCustomClassLoader(urls);
			ServiceLoader.load(Driver.class, classLoader).forEach(d -> {
				try {
					DriverManager.registerDriver(new DriverShim(d));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			try (Connection connection = DriverManager.getConnection(dbInfo.getUrl(), dbInfo.getUser(),
					dbInfo.getPassword())) {
				return Optional.ofNullable(f.apply(connection));
			} catch (SQLException e) {
				e.printStackTrace();
				return Optional.empty();
			}

		} finally {
			try {
				for (Driver d : Collections.list(DriverManager.getDrivers())) {
					if (d instanceof DriverShim) {
						DriverManager.deregisterDriver(d);
					}
				}
			} catch (SQLException e) {
			}
		}
	}

	public static <R> R query(IProject project, Connection conn, String sql, Map<String, ?> params,
			SQLFunction<ResultSet, R> fn) throws SQLException {

		try {
			List<URL> urls = new ArrayList<>();
			urls.add(Jdts.getURLFromJavaProject(project, "jp.co.future.uroborosql.SqlAgent")
					.orElseThrow(() -> new Exception("not uroboroSQL project")));

			//commons lang
			urls.add(Jdts.getURLFromJavaProject(project, "org.apache.commons.lang3.StringUtils")
					.orElseThrow(() -> new Exception("not uroboroSQL project")));

			//slf4j
			urls.add(Jdts.getURLFromJavaProject(project, "org.slf4j.Logger")
					.orElseThrow(() -> new Exception("not uroboroSQL project")));

			//javassist
			urls.add(Jdts.getURLFromJavaProject(project, "javassist.ClassPool")
					.orElseThrow(() -> new Exception("not uroboroSQL project")));

			//ognl
			urls.add(Jdts.getURLFromJavaProject(project, "ognl.Ognl")
					.orElseGet(() -> {
						try {
							return FileLocator
									.toFileURL(Internal.class.getClassLoader().getResource("lib/ognl-3.1.15.jar"));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}));
			CustomClassLoader classLoader = createCustomClassLoader(urls);
			Class<Executor> executorType = classLoader.defineAndLoadClass(UroboroSQLExecutor.class);
			Executor executor = executorType.getConstructor().newInstance();
			return executor.execute(conn, sql, params, fn);
		} catch (SQLException e) {
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
		}
		Executor executor = new JdbcExecutor();
		return executor.execute(conn, sql, params, fn);
	}

	private static URL getDriverUrlFromJavaProject(IProject project, String driver) {
		return Jdts.getURLFromJavaProject(project, driver).orElse(null);
	}

	static class DriverShim implements Driver {
		private Driver driver = null;

		DriverShim(final Driver driver) {
			this.driver = driver;
		}

		@Override
		public Connection connect(final String url, final Properties info) throws SQLException {
			return driver.connect(url, info);
		}

		@Override
		public boolean acceptsURL(final String url) throws SQLException {
			return driver.acceptsURL(url);
		}

		@Override
		public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
			return driver.getPropertyInfo(url, info);
		}

		@Override
		public int getMajorVersion() {
			return driver.getMajorVersion();
		}

		@Override
		public int getMinorVersion() {
			return driver.getMinorVersion();
		}

		@Override
		public boolean jdbcCompliant() {
			return driver.jdbcCompliant();
		}

		@Override
		public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
			return driver.getParentLogger();
		}

		@Override
		public String toString() {
			return driver.toString();
		}

	}

	public static class LabelMetadata {
		private final int[] columns;

		public LabelMetadata(int[] columns) {
			this.columns = columns;
		}

		public LabelMetadata(int column) {
			this(new int[] { column });
		}

		public LabelMetadata() {
			this(new int[0]);
		}

		public LabelMetadata(List<Integer> columns) {
			this(columns.stream().mapToInt(i -> i).toArray());
		}

		public String getString(ResultSet rs) throws SQLException {
			StringJoiner joiner = new StringJoiner(" ");
			for (int column : columns) {
				joiner.add(rs.getString(column));
			}
			String s = joiner.toString();
			return s.isEmpty() ? null : s;
		}

		public Object getObject(ResultSet rs) throws SQLException {
			if (columns.length == 0) {
				return null;
			}
			if (columns.length == 1) {
				return rs.getObject(columns[0]);
			}
			return getString(rs);
		}
	}

	public static LabelMetadata[] getLabelMetadatas(ResultSet rs, String[][] targetLabels) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		Map<String, List<Integer>> colLabels = new HashMap<>();
		for (int i = 0; i < count; i++) {
			colLabels.computeIfAbsent(metaData.getColumnLabel(i + 1), k -> new ArrayList<>()).add(i + 1);
		}
		LabelMetadata[] labels = new LabelMetadata[targetLabels.length + 1];

		for (int i = 0; i < targetLabels.length; i++) {
			String[] targetLabel = targetLabels[i];
			labels[i] = buildLabel(colLabels, targetLabel);
		}

		labels[labels.length - 1] = new LabelMetadata(colLabels.values().stream()
				.flatMap(List::stream)
				.mapToInt(i -> i)
				.toArray());

		return labels;
	}

	private static LabelMetadata buildLabel(Map<String, List<Integer>> colLabels, String[] targetLabel) {
		for (String label : targetLabel) {
			for (Map.Entry<String, List<Integer>> e : colLabels.entrySet()) {
				String k = e.getKey().replaceAll("_", "");
				String l = label.replaceAll("_", "");
				if (k.equalsIgnoreCase(l)) {
					LabelMetadata meta = new LabelMetadata(e.getValue());
					colLabels.remove(e.getKey());
					return meta;
				}
			}
		}

		//見つからない場合一番最初
		Optional<Integer> min = colLabels.values().stream()
				.flatMap(List::stream)
				.min(Comparator.naturalOrder());

		if (!min.isPresent()) {
			return new LabelMetadata();
		}

		for (Map.Entry<String, List<Integer>> e : colLabels.entrySet()) {
			if (e.getValue().contains(min.get())) {
				e.getValue().remove(min.get());
				if (e.getValue().isEmpty()) {
					colLabels.remove(e.getKey());
				}
				break;
			}
		}

		return new LabelMetadata(min.get());
	}

}
