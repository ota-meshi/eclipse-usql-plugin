package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.Objects;

public class VariableValue {
	public static final VariableValue EMPTY = new VariableValue(null, null);
	private final Object value;
	private final String literal;

	private VariableValue(Object value, String literal) {
		this.value = value;
		this.literal = literal;
	}

	public Object getValue() {
		return value;
	}

	public boolean isEmpty() {
		if (literal == null) {
			return value == null || "".equals(value);
		}
		return literal.isEmpty() || literal.equals("null") || literal.equals("''");
	}

	@Override
	public int hashCode() {
		return Objects.hash(toSqlBind());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VariableValue other = (VariableValue) obj;
		return Objects.equals(toSqlBind(), other.toSqlBind());
	}

	private static String toLiteral(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return value.toString();
		}
		return "'" + value + "'";
	}

	public String toSqlBind() {
		if (literal == null) {
			if (value == null) {
				return "''";
			}
			return toLiteral(value);
		}

		return literal;
	}

	public static VariableValue of(Object value) {
		return new VariableValue(value, null);
	}

	public static VariableValue ofLiteral(String literal) {
		return new VariableValue(null, literal);
	}

	public Object getOriginal() {
		if (literal == null) {
			return value;
		}

		if (literal.startsWith("'") && literal.endsWith("'")) {
			return literal.substring(1, literal.length() - 1);
		}
		try {
			return Long.parseLong(literal);
		} catch (NumberFormatException e) {
		}
		try {
			return Double.parseDouble(literal);
		} catch (NumberFormatException e) {
		}

		return literal;
	}
}
