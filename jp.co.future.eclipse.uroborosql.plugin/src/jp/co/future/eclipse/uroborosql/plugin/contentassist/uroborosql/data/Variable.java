package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data;

import java.util.Objects;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class Variable extends AbstractVariable {
	public Variable(String variableName, String sqlValue, String description) {
		super(variableName, sqlValue, description);
	}

	public Variable(String variableName, String sqlValue) {
		super(variableName, sqlValue);
	}

	public Variable(String variableName) {
		super(variableName);
	}

	@Override
	protected IVariable create(String variableName, String sqlValue, String description) {
		return new Variable(variableName, sqlValue, description);
	}

	@Override
	public PartContentAssistProcessor createContentAssistProcessor() {
		String text = "/*" + variableName + "*/";
		String replaceText = "/*" + variableName + "*/" + Objects.toString(sqlValue, "''");
		return new TokenContentAssistProcessor(text, replaceText, replaceText, getActDescription());
	}

	@Override
	public String toString() {
		return "/*" + variableName + "*/" + Objects.toString(sqlValue, "''");
	}

	@Override
	public String getActDescription() {
		return description != null ? description : "variable name.";
	}
}
