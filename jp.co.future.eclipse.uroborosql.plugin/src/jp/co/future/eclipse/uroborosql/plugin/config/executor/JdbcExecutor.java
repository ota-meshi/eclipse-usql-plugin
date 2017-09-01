package jp.co.future.eclipse.uroborosql.plugin.config.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import jp.co.future.eclipse.uroborosql.plugin.config.utils.SQLFunction;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;

public class JdbcExecutor implements Executor {

	@Override
	public <R> R execute(Connection conn, String sql, Map<String, ?> params, SQLFunction<ResultSet, R> fn)
			throws SQLException {
		List<Object> paramList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		Document document = new Document(sql);
		ListIterator<Token> iterator = document.getTokens().listIterator();
		while (iterator.hasNext()) {
			Token token = iterator.next();
			if (token.getType() == TokenType.M_COMMENT) {
				String comment = token.getString().replaceAll("\\*/$", "").replaceAll("^/\\*", "").trim();
				if (UroboroSQLUtils.isVariableTargetComment(comment)) {
					paramList.add(params.get(comment));
					sb.append("?");
					sb.append(token.getString());
					skipToken(iterator);
					continue;
				}
			}

			sb.append(token.getString());
		}

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 0; i < paramList.size(); i++) {
				ps.setObject(i + 1, paramList.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				return fn.apply(rs);
			}
		}
	}

	private void skipToken(ListIterator<Token> iterator) {
		while (iterator.hasNext()) {
			Token token = iterator.next();
			if (!token.getType().isVariableNext()) {
				iterator.previous();
				return;
			}
		}
	}

}
