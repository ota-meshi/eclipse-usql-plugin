package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FluentItarator<E> extends Iterator<E> {

	static final FluentItarator<?> EMPTY = FluentItarator.from(Collections.emptyIterator());

	default FluentItarator<E> filter(Predicate<? super E> predicate) {
		return Iterators.filter(this, predicate);
	}

	default Stream<E> stream() {
		return Iterators.stream(this);
	}

	static <E> FluentItarator<E> from(Iterator<E> iterator) {
		return new FluentItarator<E>() {

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
	static <E> FluentItarator<E> empty() {
		return (FluentItarator<E>) EMPTY;
	}

}