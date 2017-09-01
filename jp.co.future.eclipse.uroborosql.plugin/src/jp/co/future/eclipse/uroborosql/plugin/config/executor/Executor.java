package jp.co.future.eclipse.uroborosql.plugin.config.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import jp.co.future.eclipse.uroborosql.plugin.config.utils.SQLFunction;

public interface Executor {
	<R> R execute(Connection conn, String sql, Map<String, ?> param, SQLFunction<ResultSet, R> fn) throws SQLException;
}
