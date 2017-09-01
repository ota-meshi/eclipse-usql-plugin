package jp.co.future.eclipse.uroborosql.plugin.config.utils;

import java.sql.SQLException;

public interface SQLFunction<T, R> {

	R apply(T t) throws SQLException;
}