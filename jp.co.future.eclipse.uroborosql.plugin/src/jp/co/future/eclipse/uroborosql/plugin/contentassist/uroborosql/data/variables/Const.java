package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.LazySearchContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class Const extends AbstractVariable {
	public Const(String variableName, String sqlValue, String description) {
		super(variableName, sqlValue, description);
	}

	public Const(String variableName, String sqlValue) {
		super(variableName, sqlValue, null);
	}

	public Const(String variableName) {
		super(variableName);
	}

	public Const(String variableName, Supplier<?> sqlValueSupplier) {
		super(variableName, () -> toLiteral(sqlValueSupplier.get()));
	}

	public Const(String variableName, Supplier<?> sqlValueSupplier, Supplier<String> descriptionSupplier) {
		super(variableName, () -> toLiteral(sqlValueSupplier.get()), descriptionSupplier);
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

	@Override
	protected IVariable create(String variableName, String sqlValue, String description) {
		return new Const(variableName, sqlValue, description);
	}

	@Override
	public IPartContentAssistProcessor createContentAssistProcessor() {
		String text = "/*#" + getVariableName() + "*/";
		String replaceText = "/*#" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
		return new TokenContentAssistProcessor(text, replaceText, () -> getActDescription());
	}

	@Override
	public IPartContentAssistProcessor createLazyContentAssistProcessor() {
		List<String> texts = Stream.of(getVariableName(), getDescription())
				.filter(Objects::nonNull)
				.map(s -> "/*#" + s + "*/")
				.collect(Collectors.toList());

		String replaceText = "/*#" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
		return new LazySearchContentAssistProcessor("/*#" + getVariableName() + "*/", texts, replaceText,
				() -> getActDescription());
	}

	@Override
	public String toString() {
		return "/*#" + getVariableName() + "*/" + Objects.toString(getSqlValue(), "''");
	}

	@Override
	public String getActDescription() {
		return getDescription() != null ? getDescription() : "const name.";
	}

}
