package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.AbstractList;
import java.util.stream.Collectors;

public abstract class AbstractFluentList<E> extends AbstractList<E> implements FluentList<E> {

	@Override
	public FluentItarator<E> iterator() {
		return FluentItarator.from(super.iterator());
	}

	//TODO listIterator

	@Override
	public String toString() {
		return stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
	}
}