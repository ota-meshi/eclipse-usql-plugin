package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Iterables {

	public static <E> Iterable<E> asIterables(Supplier<Iterator<E>> itarator) {
		return () -> itarator.get();
	}

	public static <E> Stream<E> stream(Iterable<E> iterable) {
		return Iterators.stream(iterable.iterator());
	}
}
