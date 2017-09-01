package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterators {

	public static <E> Iterator<E> asIterator(E init, Function<E, Optional<E>> next) {
		return new Iterator<E>() {
			Optional<E> nextElement = next.apply(init);

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

	public static <E> Stream<E> stream(Iterator<E> iterator) {
		Spliterator<E> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
		return StreamSupport.stream(spliterator, false);
	}

}
