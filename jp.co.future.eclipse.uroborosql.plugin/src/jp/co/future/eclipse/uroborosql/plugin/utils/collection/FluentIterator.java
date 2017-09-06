package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FluentIterator<E> extends Iterator<E> {

	static final FluentIterator<?> EMPTY = FluentIterator.from(Collections.emptyIterator());

	default FluentIterator<E> filter(Predicate<? super E> predicate) {
		return Iterators.filter(this, predicate);
	}

	default Stream<E> stream() {
		return Iterators.stream(this);
	}

	static <E> FluentIterator<E> from(Iterator<E> iterator) {
		return new FluentIterator<E>() {

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
		};
	}

	@SuppressWarnings("unchecked")
	static <E> FluentIterator<E> empty() {
		return (FluentIterator<E>) EMPTY;
	}

}