package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.LazySearchContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class Variable extends AbstractVariable {
	private Variable(String variableName, VariableValue value, String description) {
		super(variableName, value, description);
	}

	public Variable(String variableName, VariableValue value) {
		super(variableName, value, (String) null);
	}

	public Variable(String variableName) {
		super(variableName);
	}

	@Override
	protected IVariable create(String variableName, VariableValue value, String description) {
		return new Variable(variableName, value, description);
	}

	@Override
	public IPartContentAssistProcessor createContentAssistProcessor() {
		String text = "/*" + getVariableName() + "*/";
		String replaceText = "/*" + getVariableName() + "*/" + getValue().toSqlBind();
		return new TokenContentAssistProcessor(text, replaceText, false, () -> getActDescription());
	}

	@Override
	public IPartContentAssistProcessor createLazyContentAssistProcessor() {
		List<String> texts = Stream.of(getVariableName(), getDescription())
				.filter(Objects::nonNull)
				.map(s -> "/*" + s + "*/")
				.collect(Collectors.toList());
		String replaceText = "/*" + getVariableName() + "*/" + getValue().toSqlBind();
		return new LazySearchContentAssistProcessor("/*" + getVariableName() + "*/", texts, replaceText, false,
				() -> getActDescription());
	}

	@Override
	public String toString() {
		return "/*" + getVariableName() + "*/" + getValue().toSqlBind();
	}

	@Override
	public String getActDescription() {
		return getDescription() != null ? getDescription() : "variable name.";
	}

}
