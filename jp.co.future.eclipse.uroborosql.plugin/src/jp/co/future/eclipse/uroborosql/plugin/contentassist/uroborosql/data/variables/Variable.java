package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.LazySearchContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class Variable extends AbstractVariable {
	private Variable(String variableName, String sqlValue, String description) {
		super(variableName, sqlValue, description);
	}

	public Variable(String variableName, String sqlValue) {
		super(variableName, sqlValue, null);
	}

	public Variable(String variableName) {
		super(variableName);
	}

	@Override
	protected IVariable create(String variableName, String sqlValue, String description) {
		return new Variable(variableName, sqlValue, description);
	}

	@Override
	public IPartContentAssistProcessor createContentAssistProcessor() {
		String text = "/*" + getVariableName() + "*/";
		String replaceText = "/*" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
		return new TokenContentAssistProcessor(text, replaceText, false, () -> getActDescription());
	}

	@Override
	public IPartContentAssistProcessor createLazyContentAssistProcessor() {
		List<String> texts = Stream.of(getVariableName(), getDescription())
				.filter(Objects::nonNull)
				.map(s -> "/*" + s + "*/")
				.collect(Collectors.toList());
		String replaceText = "/*" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
		return new LazySearchContentAssistProcessor("/*" + getVariableName() + "*/", texts, replaceText, false,
				() -> getActDescription());
	}

	@Override
	public String toString() {
		return "/*" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
	}

	@Override
	public String getActDescription() {
		return getDescription() != null ? getDescription() : "variable name.";
	}

}
