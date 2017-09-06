package jp.co.future.eclipse.uroborosql.plugin.config;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers.Columns;
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
	public Columns getColumn(Table table) {
		return new Columns();
	}
}
