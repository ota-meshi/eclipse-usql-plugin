package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FluentList<E> extends List<E>, FluentIterable<E> {
	static final FluentList<?> EMPTY = from(Collections.emptyList());

	@Override
	default FluentListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	FluentListIterator<E> listIterator(int index);

	@Override
	default OptionalInt findLastIndex(Predicate<? super E> predicate) {
		Iterator<E> itr = reverseIterator();
		int index = size() - 1;
		while (itr.hasNext()) {
			if (predicate.test(itr.next())) {
				return OptionalInt.of(index);
			}
			index--;
		}
		return OptionalInt.empty();
	}

	@Override
	default Stream<E> stream() {
		return List.super.stream();
	}

	@Override
	default <R> FluentList<R> map(Function<? super E, ? extends R> mapper) {
		return new AbstractFluentList<R>() {

			@Override
			public R get(int index) {
				return mapper.apply(FluentList.this.get(index));
			}

			@Override
			public int size() {
				return FluentList.this.size();
			}
		};
	}

	default FluentList<E> skip(long n) {
		int i = (int) n;
		return new AbstractFluentList<E>() {

			@Override
			public E get(int index) {
				return FluentList.this.get(index + i);
			}

			@Override
			public int size() {
				return FluentList.this.size() - i;
			}
		};
	}

	@SuppressWarnings("unchecked")
	static <E> FluentList<E> empty() {
		return (FluentList<E>) EMPTY;
	}

	@SafeVarargs
	static <E> FluentList<E> of(E... values) {
		return from(Arrays.asList(values));
	}

	static <E> FluentList<E> from(List<E> list) {
		return new AbstractFluentList<E>() {

			@Override
			public E get(int index) {
				return list.get(index);
			}

			@Override
			public int size() {
				return list.size();
			}
		};
	}

}