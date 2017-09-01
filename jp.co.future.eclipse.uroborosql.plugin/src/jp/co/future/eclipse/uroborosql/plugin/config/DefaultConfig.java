package jp.co.future.eclipse.uroborosql.plugin.config;

import java.util.Collections;
import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Column;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Table;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Tables;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Variables;

public class DefaultConfig implements PluginConfig {
	private static DefaultConfig instance;

	public static DefaultConfig getInstance() {
		return instance != null ? instance : (instance = new DefaultConfig());
	}

	@Override
	public String getSqlId() {
		return PluginConfig.super.getSqlId();
	}

	@Override
	public Variables getConsts() {
		return PluginConfig.super.getConsts();
	}

	@Override
	public Tables getTables(String text, boolean lazy) {
		return new Tables();
	}

	@Override
	public List<Column> getColumn(Table table) {
		return Collections.emptyList();
	}
}
