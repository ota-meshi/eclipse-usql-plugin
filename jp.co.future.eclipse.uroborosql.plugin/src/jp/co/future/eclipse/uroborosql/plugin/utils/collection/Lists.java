package jp.co.future.eclipse.uroborosql.plugin.utils.collection;

public class Lists {

	@SafeVarargs
	public static <E> FluentList<E> asList(E... array) {
		return new AbstractFluentList<E>() {

			@Override
			public E get(int index) {
				return array[index];
			}

			@Override
			public int size() {
				return array.length;
			}

			@Override
			public E set(int index, E element) {
				E old = array[index];
				array[index] = element;
				return old;
			}
		};
	}

}
