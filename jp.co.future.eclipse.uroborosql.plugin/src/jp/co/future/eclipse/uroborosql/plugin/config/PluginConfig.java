package jp.co.future.eclipse.uroborosql.plugin.config;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;

public interface PluginConfig {
	default String getSqlId() {
		return "_SQL_ID_";
	}

	default Map<String, ?> getConsts() {
		return Collections.emptyMap();
	}

	public static PluginConfig load(IEditorPart editor) {
		IProject project = Internal.getProject(editor);
		return load(project);
	}

	public static PluginConfig load(IProject project) {
		return Internal.getConfig(project);
	}

}
