package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;

public interface IVariable {

	IPartContentAssistProcessor createContentAssistProcessor();

	IPartContentAssistProcessor createLazyContentAssistProcessor();

	String getVariableName();

	VariableValue getValue();

	String getDescription();

	String getActDescription();

	IVariable marge(IVariable value);
}
