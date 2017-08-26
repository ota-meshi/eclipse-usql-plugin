package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data;

import java.util.Objects;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class Const extends AbstractVariable {
	public Const(String variableName, String sqlValue, String description) {
		super(variableName, sqlValue, description);
	}

	public Const(String variableName, String sqlValue) {
		super(variableName, sqlValue);
	}

	public Const(String variableName) {
		super(variableName);
	}

	public Const(String variableName, Object sqlValue) {
		this(variableName, toLiteral(sqlValue));
	}

	public Const(String variableName, Object sqlValue, String description) {
		this(variableName, toLiteral(sqlValue), description);
	}

	private static String toLiteral(Object value) {
		if (value == null) {
			return "''";
		}
		if (value instanceof Number) {
			return value.toString();
		}
		return "'" + value + "'";
	}

	@Override
	protected IVariable create(String variableName, String sqlValue, String description) {
		return new Const(variableName, sqlValue, description);
	}

	@Override
	public PartContentAssistProcessor createContentAssistProcessor() {
		String text = "/*#" + variableName + "*/";
		String replaceText = "/*#" + variableName + "*/" + Objects.toString(sqlValue, "''");
		return new TokenContentAssistProcessor(text, replaceText, replaceText, getActDescription());
	}

	@Override
	public String toString() {
		return "/*#" + variableName + "*/" + Objects.toString(sqlValue, "''");
	}

	@Override
	public String getActDescription() {
		return description != null ? description : "const name.";
	}

}
