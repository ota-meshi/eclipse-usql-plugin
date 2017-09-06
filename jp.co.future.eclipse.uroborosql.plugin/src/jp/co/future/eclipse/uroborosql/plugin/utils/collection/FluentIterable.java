package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Arrays;
import java.util.Optional;
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
		E last = null;
		for (E e : this) {
			last = e;
		}
		return Optional.ofNullable(last);
	}

	default Optional<E> findFirst() {
		for (E e : this) {
			return Optional.of(e);
		}
		return Optional.empty();
	}

	default <R, A> R collect(Collector<? super E, A, R> collector) {
		return stream().collect(collector);
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