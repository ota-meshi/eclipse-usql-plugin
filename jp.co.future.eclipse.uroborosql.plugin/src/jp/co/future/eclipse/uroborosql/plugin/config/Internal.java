package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.xml.sax.SAXException;

class Internal {
	public static IProject getProject(IEditorPart editor) {
		IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		IFile file = editorInput.getFile();

		return file.getProject();
	}

	public static PluginConfig getConfig(IProject project) {
		try {
			Path location = Paths.get(project.getDescription().getLocationURI());
			Path path = location.resolve(".uroborosqlpluginrc.xml");
			if (Files.exists(path)) {
				try (InputStream input = Files.newInputStream(path)) {
					return new XmlConfig(input, project);
				}
			}

			return null;
		} catch (CoreException | IOException | ParserConfigurationException | SAXException e) {
		}
		return getDefaultConfig();
	}

	public static PluginConfig getDefaultConfig() {

		return new PluginConfig() {
			//default
		};
	}

	static abstract class AbsJavaData {
		private final Collection<URL> urls;

		AbsJavaData(Collection<URL> urls) {
			this.urls = urls;
		}

		public ClassLoader createURLClassLoader() {
			return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
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

	public interface SQLFunction<R> {

		R apply(Connection conn) throws SQLException;
	}

	public static <R> Optional<R> connect(IProject project, String driver, String url, String user, String password,
			List<String> classpaths,
			SQLFunction<R> f) {

		URL classUrl = getDriverUrlFromJavaProject(project, driver);
		List<URL> urls = new ArrayList<>();
		if (classUrl == null) {
			if (classpaths.isEmpty()) {
				return Optional.empty();
			}
		} else {
			urls.add(classUrl);
		}
		for (String classpath : classpaths) {
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

		try {
			URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
			ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class, classLoader);
			loader.forEach(d -> {
				try {
					DriverManager.registerDriver(new DriverShim(d));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			try (Connection connection = DriverManager.getConnection(url, user, password)) {
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

	private static URL getDriverUrlFromJavaProject(IProject project, String driver) {
		if (driver == null) {
			return null;
		}

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return null;
		}
		try {

			IType type = javaProject.findType(driver);

			if (type == null) {
				return null;
			}
			IClassFile cf = type.getClassFile();
			if (cf == null) {
				return null;
			}
			return cf.getPath().toFile().toURI().toURL();
		} catch (JavaModelException | MalformedURLException e) {
			return null;
		}
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
}
