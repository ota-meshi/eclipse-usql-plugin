package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.IOException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
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
				return new XmlConfig(path, project);
			}

			return null;
		} catch (CoreException | IOException | ParserConfigurationException | SAXException e) {
		}
		return new PluginConfig() {
			//default
		};
	}

	static class ClassesData {
		private final List<URL> urls;
		private final List<String> loaderTargetClassNames;
		private final List<IType> sourceTypes;

		public ClassesData(List<URL> urls, List<String> loaderTargetClassNames, List<IType> sourceTypes) {
			this.urls = urls;
			this.loaderTargetClassNames = loaderTargetClassNames;
			this.sourceTypes = sourceTypes;
		}

		public List<URL> getUrls() {
			return urls;
		}

		public List<IType> getSourceTypes() {
			return sourceTypes;
		}

		public List<String> getLoaderTargetClassNames() {
			return loaderTargetClassNames;
		}

		public ClassLoader createURLClassLoader() {
			return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
		}

	}

	public static ClassesData getClassesData(IProject project, List<String> classNames) {
		IJavaProject javaProject = JavaCore.create(project);

		List<URL> urls = new ArrayList<>();
		List<String> loaderTargetClassNames = new ArrayList<>();
		List<IType> sourceTypes = new ArrayList<>();
		for (String className : classNames) {
			try {
				IType type = javaProject.findType(className);

				if (type == null) {
					continue;
				}
				IClassFile cf = type.getClassFile();
				if (cf != null) {
					URL url = cf.getPath().toFile().toURI().toURL();
					urls.add(url);
					loaderTargetClassNames.add(className);
				} else {
					sourceTypes.add(type);
				}
			} catch (JavaModelException | MalformedURLException e) {
			}
		}
		return new ClassesData(urls, loaderTargetClassNames, sourceTypes);
	}

	static class PackagesData {
	}

	public static PackagesData getPackagesData(IProject project, List<String> packageNames) {
		return new PackagesData();
		//TODO どうやって取得するの？
		//		for (String pkg : packages) {
		//			try {
		//				for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {
		//					if (!packageFragment.getElementName().equals(pkg)) {
		//						continue;
		//					}
		//
		//					for (IClassFile cf : packageFragment.getClassFiles()) {
		//						try {
		//							URL url = cf.getPath().toFile().toURI().toURL();
		//							urls.add(url);
		//						} catch (MalformedURLException e) {
		//						}
		//					}
		//
		//				}
		//
		//			} catch (JavaModelException e) {
		//			}
		//		}
	}

	public interface SQLFunction<R> {

		R apply(Connection conn) throws SQLException;
	}

	public static <R> Optional<R> connect(IProject project, String driver, String url, String user, String password,
			SQLFunction<R> f) {
		IJavaProject javaProject = JavaCore.create(project);

		URL classUrl;
		try {
			IType type = javaProject.findType(driver);

			if (type == null) {
				return Optional.empty();
			}
			IClassFile cf = type.getClassFile();
			if (cf == null) {
				return Optional.empty();
			}
			classUrl = cf.getPath().toFile().toURI().toURL();
		} catch (JavaModelException | MalformedURLException e) {
			return Optional.empty();
		}

		//		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
			//			Thread.currentThread().setContextClassLoader(classLoader);
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
			//Thread.currentThread().setContextClassLoader(currentClassLoader);
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
