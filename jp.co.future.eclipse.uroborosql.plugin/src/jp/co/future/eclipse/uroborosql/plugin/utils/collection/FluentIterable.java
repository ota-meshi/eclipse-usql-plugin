package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface FluentIterable<E> extends Iterable<E> {
	static final FluentIterable<?> EMPTY = () -> FluentItarator.empty();

	@Override
	FluentItarator<E> iterator();

	default FluentIterable<E> filter(Predicate<? super E> predicate) {
		return Iterables.filter(this, predicate);
	}

	default <R> FluentIterable<R> map(Function<? super E, ? extends R> mapper) {
		return Iterables.map(this, mapper);
	}

	default Stream<E> stream() {
		return Iterables.stream(this);
	}

	default Optional<E> findLast() {
		Iterator<E> itr = iterator();
		if (itr.hasNext()) {
			E e = itr.next();
			while (itr.hasNext()) {
				e = itr.next();
			}
			return Optional.of(e);
		}
		return Optional.empty();
	}

	default Optional<E> findFirst() {
		Iterator<E> itr = iterator();
		if (itr.hasNext()) {
			return Optional.of(itr.next());
		}
		return Optional.empty();
	}

	default OptionalInt findLastIndex() {
		Iterator<E> itr = iterator();
		if (itr.hasNext()) {
			int index = 0;
			itr.next();
			while (itr.hasNext()) {
				index++;
				itr.next();
			}
			return OptionalInt.of(index);
		}
		return OptionalInt.empty();
	}

	default OptionalInt findFirstIndex() {
		Iterator<E> itr = iterator();
		if (itr.hasNext()) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(0);
	}

	default <R, A> R collect(Collector<? super E, A, R> collector) {
		return stream().collect(collector);
	}

	default boolean anyMatch(Predicate<? super E> predicate) {
		return stream().anyMatch(predicate);
	}

	default boolean allMatch(Predicate<? super E> predicate) {
		return stream().allMatch(predicate);
	}

	default boolean noneMatch(Predicate<? super E> predicate) {
		return stream().noneMatch(predicate);
	}

	@SuppressWarnings("unchecked")
	static <E> FluentIterable<E> empty() {
		return (FluentIterable<E>) EMPTY;
	}

	@SafeVarargs
	static <E> FluentIterable<E> of(E... values) {
		return from(Arrays.asList(values));
	}

	static <E> FluentIterable<E> from(Iterable<E> iterable) {
		return new AbstractFluentIterable<E>() {

			@Override
			public FluentItarator<E> iterator() {
				return FluentItarator.from(iterable.iterator());
			}
		};
	}
}