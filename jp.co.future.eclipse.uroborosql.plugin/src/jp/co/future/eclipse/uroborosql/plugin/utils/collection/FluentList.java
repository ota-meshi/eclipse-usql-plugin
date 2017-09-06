package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface FluentList<E> extends List<E>, FluentIterable<E> {
	static final FluentList<?> EMPTY = from(Collections.emptyList());

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