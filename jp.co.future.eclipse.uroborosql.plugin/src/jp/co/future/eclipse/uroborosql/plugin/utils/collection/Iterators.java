package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterators {

	public static <E> FluentItarator<E> asIterator(E init, Function<E, Optional<E>> next) {
		return new FluentItarator<E>() {
			Optional<E> nextElement = Optional.ofNullable(init);

			@Override
			public E next() {
				E result = nextElement.get();
				nextElement = next.apply(result);
				return result;
			}

			@Override
			public boolean hasNext() {
				return nextElement.isPresent();
			}
		};
	}

	public static <E> FluentItarator<E> asIteratorFromNext(E init, Function<E, Optional<E>> next) {

		Optional<E> nextElement = next.apply(init);
		if (!nextElement.isPresent()) {
			return FluentItarator.empty();
		}
		return asIterator(nextElement.get(), next);
	}

	public static <E> Stream<E> stream(Iterator<E> iterator) {
		Spliterator<E> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
		return StreamSupport.stream(spliterator, false);
	}

	public static <E> FluentItarator<E> filter(Iterator<E> iterator, Predicate<? super E> predicate) {
		return FluentItarator.from(stream(iterator).filter(predicate).iterator());
	}

}
