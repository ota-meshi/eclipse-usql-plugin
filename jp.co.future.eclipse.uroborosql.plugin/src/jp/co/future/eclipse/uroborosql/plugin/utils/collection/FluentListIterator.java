package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Collections;
import java.util.ListIterator;

public interface FluentListIterator<E> extends FluentIterator<E>, ListIterator<E> {

	static final FluentListIterator<?> EMPTY = FluentListIterator.from(Collections.emptyListIterator());

	static <E> FluentListIterator<E> from(ListIterator<E> iterator) {
		return new FluentListIterator<E>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public E next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				iterator.remove();
			}

			@Override
			public boolean hasPrevious() {
				return iterator.hasPrevious();
			}

			@Override
			public E previous() {
				return iterator.previous();
			}

			@Override
			public int nextIndex() {
				return iterator.nextIndex();
			}

			@Override
			public int previousIndex() {
				return iterator.previousIndex();
			}

			@Override
			public void set(E e) {
				iterator.set(e);
			}

			@Override
			public void add(E e) {
				iterator.add(e);
			}
		};
	}

	@SuppressWarnings("unchecked")
	static <E> FluentListIterator<E> empty() {
		return (FluentListIterator<E>) EMPTY;
	}

}