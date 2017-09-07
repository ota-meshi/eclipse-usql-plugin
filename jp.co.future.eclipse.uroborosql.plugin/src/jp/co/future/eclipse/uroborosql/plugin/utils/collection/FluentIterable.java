package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface FluentIterable<E> extends Iterable<E> {
	static final FluentIterable<?> EMPTY = () -> FluentIterator.empty();

	@Override
	FluentIterator<E> iterator();

	default FluentIterable<E> filter(Predicate<? super E> predicate) {
		return Iterables.filter(this, predicate);
	}

	@SuppressWarnings("unchecked")
	default FluentIterator<E> reverseIterator() {
		List<E> list;
		if (this instanceof List) {
			list = (List<E>) this;
		} else {
			list = collect(Collectors.toList());
		}
		ListIterator<E> listIterator = list.listIterator(list.size());
		return Iterators.asIterator(listIterator::hasPrevious, listIterator::previous);
	}

	default <R> FluentIterable<R> map(Function<? super E, ? extends R> mapper) {
		return Iterables.map(this, mapper);
	}

	default FluentIterable<E> sorted(Comparator<? super E> comparator) {
		return Iterables.sorted(this, comparator);
	}

	default FluentIterable<E> distinct() {
		return Iterables.distinct(this);
	}

	default Stream<E> stream() {
		return Iterables.stream(this);
	}

	default Optional<E> findLast() {
		Iterator<E> itr = reverseIterator();
		if (itr.hasNext()) {
			return Optional.of(itr.next());
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

	default OptionalInt findLastIndex(Predicate<? super E> predicate) {
		Iterator<E> itr = reverseIterator();
		int count = 0;
		while (itr.hasNext()) {
			if (predicate.test(itr.next())) {
				int idx = count;
				count++;
				while (itr.hasNext()) {
					itr.next();
					count++;
				}
				return OptionalInt.of(count - idx - 1);
			}
			count++;
		}
		return OptionalInt.empty();
	}

	default OptionalInt findFirstIndex(Predicate<? super E> predicate) {
		Iterator<E> itr = iterator();
		int index = 0;
		while (itr.hasNext()) {
			if (predicate.test(itr.next())) {
				return OptionalInt.of(index);
			}
			index++;
		}
		return OptionalInt.empty();
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

	static <E> FluentIterable<E> from(Iterable<? extends E> iterable) {
		return new AbstractFluentIterable<E>() {

			@Override
			public FluentIterator<E> iterator() {
				return FluentIterator.from(iterable.iterator());
			}
		};
	}

}