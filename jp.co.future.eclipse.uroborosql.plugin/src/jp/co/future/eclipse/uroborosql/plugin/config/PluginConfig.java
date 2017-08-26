package jp.co.future.eclipse.uroborosql.plugin.config;

import org.eclipse.core.resources.IProject;
import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorPlugin;
import org.eclipse.ui.IEditorPart;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.Variables;

public interface PluginConfig {
	default String getSqlId() {
		return "_SQL_ID_";
	}

	default Variables getConsts() {
		return new Variables();
	}

	public static PluginConfig load() {
		try {
			IEditorPart editor = SQLEditorPlugin.getActiveEditor();
			return load(editor);
		} catch (Throwable e) {
			return Internal.getDefaultConfig();
		}
	}

	public static PluginConfig load(IEditorPart editor) {
		IProject project = Internal.getProject(editor);
		return load(project);
	}

	public static PluginConfig load(IProject project) {
		return Internal.getConfig(project);
	}

}
