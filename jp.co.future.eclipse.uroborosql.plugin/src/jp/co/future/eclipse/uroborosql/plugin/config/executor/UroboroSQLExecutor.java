package jp.co.future.eclipse.uroborosql.plugin.config.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import jp.co.future.eclipse.uroborosql.plugin.config.utils.SQLFunction;

public class UroboroSQLExecutor implements Executor {

	@Override
	public <R> R execute(Connection conn, String sql, Map<String, ?> param, SQLFunction<ResultSet, R> fn)
			throws SQLException {
		jp.co.future.uroborosql.config.SqlConfig config = jp.co.future.uroborosql.config.DefaultSqlConfig
				.getConfig(conn);

		try (jp.co.future.uroborosql.SqlAgent agent = config.createAgent()) {

			try (ResultSet rs = agent.queryWith(sql).paramMap(param).resultSet()) {
				return fn.apply(rs);
			}
		}
	}

}
