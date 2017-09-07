package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IBranch;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IMethod;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.NodeLevel;
import jp.co.future.eclipse.uroborosql.plugin.utils.Jdts;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentIterable;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterables;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

public class Branch extends AbstractNamedNode<Branch> implements IBranch {
	private final Map<String, IBranch> fields = new HashMap<>();
	private final Map<String, Collection<IMethod>> methods = new HashMap<>();
	private Supplier<Iterator<IMethod>> methodsGetter;
	private Supplier<Iterator<IBranch>> fieldsGetter;
	private final String display;

	public Branch(NodeLevel level, String name) {
		super(level, name);
		methodsGetter = () -> Collections.emptyIterator();
		fieldsGetter = () -> Collections.emptyIterator();
		display = null;
	}

	public Branch(NodeLevel level, String name, Class<?> type) {
		super(level, name);
		methodsGetter = createMethodsGetter(type);
		fieldsGetter = createFieldsGetter(type);
		display = name + " : " + type.getSimpleName();
		setClassName(type.getName());
	}

	@Override
	public boolean isEmpty() {
		return fields.isEmpty() && methods.isEmpty() && !methodsGetter.get().hasNext() && !fieldsGetter.get().hasNext();
	}

	private Supplier<Iterator<IMethod>> createMethodsGetter(Class<?> type) {
		return () -> {
			java.lang.reflect.Method[] methods = type.getMethods();
			return Arrays.stream(methods).map(IMethod::of).iterator();
		};
	}

	private Supplier<Iterator<IBranch>> createFieldsGetter(Class<?> type) {
		return () -> {
			java.lang.reflect.Field[] fields = type.getFields();
			return Arrays.stream(fields).<IBranch> map(IBranch::of).iterator();
		};
	}

	@Override
	public FluentIterable<INamedNode> children() {
		return Iterables.concat(
				fields.values(),
				Iterables.asIterables(fieldsGetter),
				() -> methods.values().stream()
						.<INamedNode> flatMap(vals -> vals.stream())
						.iterator(),
				Iterables.asIterables(methodsGetter));
	}

	@Override
	public AssistText createAssistText() {
		return new AssistText(name(), name().length(), 0);
	}

	@Override
	public boolean isMatchToken(String token) {
		return name().equals(token);
	}

	public IMethod putMethod(IMethod node) {
		fields.computeIfPresent(node.name(), (k, f) -> {
			if (f.nodeLevel() == NodeLevel.UNKNOWN && f.isEmpty()) {
				return null;
			}
			return f;
		});

		Collection<IMethod> methodList = methods.computeIfAbsent(node.name(), k -> new ArrayList<>());
		Iterator<IMethod> itr = methodList.iterator();
		while (itr.hasNext()) {
			IMethod old = itr.next();
			if (old.equals(node)) {
				//test
				if (old.nodeLevel().compareTo(node.nodeLevel()) >= 0) {
					old.marge(node);
					return old;
				} else {
					node.marge(old);
					itr.remove();
				}
				break;
			}
		}
		methodList.add(node);

		return node;

	}

	public IMethod putMethod(List<String> parentNames, IMethod node) {
		IBranch branch = this;
		for (String name : parentNames) {
			branch = branch.getOrCreateBranch(name);
		}
		return ((Branch) branch).putMethod(node);
	}

	public IBranch putField(IBranch node) {
		return fields.compute(node.name(), (k, old) -> {
			if (old == null) {
				return node;
			}
			if (old.nodeLevel().compareTo(node.nodeLevel()) >= 0) {
				old.marge(node);
				return old;
			} else {
				node.marge(old);
				return node;
			}
		});
	}

	public IBranch putField(List<String> parentNames, IBranch node) {
		IBranch branch = this;
		for (String name : parentNames) {
			branch = branch.getOrCreateBranch(name);
		}
		return ((Branch) branch).putField(node);
	}

	@Override
	public IBranch getOrCreateBranch(String name) {
		return fields.computeIfAbsent(name, k -> {
			return IBranch.ofUnknown(name);
		});
	}

	@Override
	protected void marge0(INamedNode node) {
		if (node instanceof Branch) {
			Branch b = (Branch) node;

			b.fields.values().forEach(this::putField);
			b.methods.values().forEach(c -> c.forEach(this::putMethod));

			Supplier<Iterator<IMethod>> myMethodsGetter = methodsGetter;
			Supplier<Iterator<IBranch>> myFieldsGetter = fieldsGetter;
			methodsGetter = () -> Iterators.concat(myMethodsGetter.get(), b.methodsGetter.get());
			fieldsGetter = () -> Iterators.concat(myFieldsGetter.get(), b.fieldsGetter.get());
		}
	}

	public void putChild(INamedNode node) {
		if (node instanceof IMethod) {
			putMethod((IMethod) node);
		} else if (node instanceof IBranch) {
			putField((IBranch) node);
		} else {
			throw new IllegalArgumentException(node.getClass().getName());
		}
	}

	public void putChild(List<String> parents, INamedNode node) {
		if (node instanceof IMethod) {
			putMethod(parents, (IMethod) node);
		} else if (node instanceof IBranch) {
			putField(parents, (IBranch) node);
		} else {
			throw new IllegalArgumentException(node.getClass().getName());
		}

	}

	@Override
	public String toDisplayString() {
		return display != null ? display : createAssistText().getReplacementString();
	}

	public Branch setClassName(String className) {
		setGetAdditionalProposalInfo(() -> Jdts.getJavadocHtml(className));
		return this;
	}
}
