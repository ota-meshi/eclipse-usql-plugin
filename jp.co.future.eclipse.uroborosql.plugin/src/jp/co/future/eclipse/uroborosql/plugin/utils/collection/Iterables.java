package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Iterables {
	public static <E> FluentIterable<E> asIterables(Supplier<? extends Iterator<E>> itarator) {
		return FluentIterable.from(() -> itarator.get());
	}

	public static <E> FluentIterable<E> filter(Iterable<E> iterable, Predicate<? super E> predicate) {
		return FluentIterable.from(() -> stream(iterable).filter(predicate).iterator());
	}

	public static <E, R> FluentIterable<R> map(Iterable<E> iterable, Function<? super E, ? extends R> mapper) {
		return FluentIterable.from(() -> stream(iterable).<R> map(mapper).iterator());
	}

	public static <E> FluentIterable<E> sorted(Iterable<E> iterable, Comparator<? super E> comparator) {
		return FluentIterable.from(() -> stream(iterable).sorted(comparator).iterator());
	}

	public static <E> FluentIterable<E> distinct(Iterable<E> iterable) {
		return FluentIterable.from(() -> stream(iterable).distinct().iterator());
	}

	public static <E> Stream<E> stream(Iterable<E> iterable) {
		return Iterators.stream(iterable.iterator());
	}

	@SafeVarargs
	public static <E> FluentIterable<E> concat(Iterable<? extends E>... iterables) {
		@SuppressWarnings("unchecked")
		Supplier<Iterator<? extends E>>[] sup = new Supplier[iterables.length];
		for (int i = 0; i < sup.length; i++) {
			sup[i] = iterables[i]::iterator;
		}

		return FluentIterable.from(() -> Iterators.concat(sup));
	}

}
