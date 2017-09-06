package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.AbstractList;
import java.util.stream.Collectors;

public abstract class AbstractFluentList<E> extends AbstractList<E> implements FluentList<E> {

	@Override
	public FluentIterator<E> iterator() {
		return FluentIterator.from(super.iterator());
	}

	@Override
	public String toString() {
		return stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public FluentListIterator<E> listIterator() {
		return FluentList.super.listIterator();
	}

	@Override
	public FluentListIterator<E> listIterator(int index) {
		return FluentListIterator.from(super.listIterator(index));
	}

}