package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.util.HashMap;
import java.util.Map;

public class Maps {
	public interface MapBuilder<K, V> extends Map<K, V> {
		MapBuilder<K, V> p(K k, V v);
	}

	private static class MapBuilderImpl<K, V> extends HashMap<K, V> implements MapBuilder<K, V> {
		public MapBuilderImpl(K k, V v) {
			put(k, v);
		}

		@Override
		public MapBuilder<K, V> p(K k, V v) {
			put(k, v);
			return this;
		}
	}

	public static <K, V> MapBuilder<K, V> of(K k, V v) {
		return new MapBuilderImpl<>(k, v);
	}

}
