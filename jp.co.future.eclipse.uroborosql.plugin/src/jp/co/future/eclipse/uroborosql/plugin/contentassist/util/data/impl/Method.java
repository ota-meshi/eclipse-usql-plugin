package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IBranch;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IDataType;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IMethod;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.NodeLevel;
import jp.co.future.eclipse.uroborosql.plugin.utils.Jdts;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentIterable;

public class Method extends AbstractNamedNode<Method> implements IMethod {
	private final IDataType result;
	private final String[] args;
	private final Supplier<String> display;

	public Method(NodeLevel level, String name, IDataType result, String... args) {
		super(level, name);
		this.result = result;
		this.args = args;
		display = null;
	}

	public Method(NodeLevel level, String name, java.lang.reflect.Method method) {
		super(level, name);
		result = IDataType.of(method.getReturnType());
		args = Arrays.stream(method.getParameters())
				.map(p -> p.getName())
				.toArray(String[]::new);
		display = () -> toDisplay(method);

		setClassName(method.getDeclaringClass().getName());
	}

	private String toDisplay(java.lang.reflect.Method method) {

		List<String> args = new ArrayList<>();
		for (int i = 0; i < this.args.length; i++) {
			args.add(method.getParameterTypes()[i].getSimpleName() + " " + this.args[i]);
		}

		return method.getName() + "(" + String.join(", ", args) + ") : " + method.getReturnType().getSimpleName()
				+ " - " + method.getDeclaringClass().getSimpleName();
	}

	@Override
	public AssistText createAssistText() {
		String text = name() + "(";
		String argsText = Arrays.stream(args)
				.map(s -> "_" + s)
				.collect(Collectors.joining(", "));

		return new AssistText(text + argsText + ")", text.length(), argsText.length());
	}

	@Override
	public FluentIterable<INamedNode> children() {
		if (result instanceof IBranch) {
			return ((IBranch) result).children();
		}
		return FluentIterable.empty();
	}

	@Override
	public boolean isMatchToken(String token) {

		Pattern pattern = Pattern.compile("^(.*)\\((.*)\\)$");
		Matcher matcher = pattern.matcher(token);
		if (matcher.matches()) {
			if (!name().equals(matcher.group(1))) {
				return false;
			}
			if (matcher.group(2).split(",").length != args.length) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name(), args.length);
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
		Method other = (Method) obj;
		return Objects.equals(name(), other.name())
				&& Objects.equals(args.length, other.args.length);
	}

	@Override
	protected void marge0(INamedNode node) {
		if (equals(node)) {
			Method m = (Method) node;

			margeArgs(m.args);

		}

	}

	@Override
	public String toDisplayString() {
		return display != null ? display.get() : createAssistText().getReplacementString();
	}

	public Method setClassName(String className) {
		setGetAdditionalProposalInfo(() -> Jdts.getMethodJavadocHtml(className, name(), args.length));
		margeArgs(Jdts.getMethodtParameterNames(className, name(), args.length));
		return this;
	}

	private void margeArgs(String[] args) {
		if (args == null) {
			return;
		}
		int len = Math.min(this.args.length, args.length);
		for (int i = 0; i < len; i++) {
			if (this.args[i] == null) {
				this.args[i] = args[i];
			} else if (this.args[i].matches("arg\\d+")) {
				this.args[i] = args[i];
			}
		}
	}
}
