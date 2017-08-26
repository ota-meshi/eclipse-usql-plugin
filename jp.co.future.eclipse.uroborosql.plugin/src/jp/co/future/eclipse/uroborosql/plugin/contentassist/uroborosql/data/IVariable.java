package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;

public interface IVariable {

	PartContentAssistProcessor createContentAssistProcessor();

	String getVariableName();

	String getSqlValue();

	String getDescription();

	String getActDescription();

	IVariable marge(IVariable value);
}
