package jp.co.future.eclipse.uroborosql.plugin.config;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Columns;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Table;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Tables;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Variables;
import jp.co.future.eclipse.uroborosql.plugin.utils.Eclipses;

public interface PluginConfig {
	interface DbInfos {
		DbInfo get(String name);

		DbInfo get();
	}

	static class DbInfo {
		private final String url;
		private final String user;
		private final String password;
		private final String driver;
		private final List<String> classpaths;

		public DbInfo(String url, String user, String password, String driver, List<String> classpaths) {
			this.url = url;
			this.user = user;
			this.password = password;
			this.driver = driver;
			this.classpaths = classpaths;
		}

		public String getUrl() {
			return url;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public String getDriver() {
			return driver;
		}

		public List<String> getClasspaths() {
			return classpaths;
		}
	}

	default String getSqlId() {
		return "_SQL_ID_";
	}

	default Variables getConsts() {
		return new Variables();
	}

	Tables getTables(String text, boolean lazy);

	Columns getColumn(Table table);

	default DbInfo getDbInfo(String name) {
		DbInfo db = getDbInfos().get(name);
		if (db == null) {
			db = getDbInfos().get();
		}
		return db;
	}

	default DbInfos getDbInfos() {
		return new DbInfos() {

			@Override
			public DbInfo get() {
				return null;
			}

			@Override
			public DbInfo get(String name) {
				return null;
			}
		};
	}

	public static PluginConfig load() {
		try {
			IEditorPart editor = Eclipses.getActiveEditor();
			return load(editor);
		} catch (Throwable e) {
			return DefaultConfig.getInstance();
		}
	}

	public static PluginConfig load(IEditorPart editor) {
		IProject project = Eclipses.getProject(editor);
		return load(project);
	}

	public static PluginConfig load(IProject project) {
		return Internal.getConfig(project);
	}

}
