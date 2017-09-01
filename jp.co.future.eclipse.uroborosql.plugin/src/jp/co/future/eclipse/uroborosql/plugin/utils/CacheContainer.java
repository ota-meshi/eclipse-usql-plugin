package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheContainer<E, ERR extends Exception> {
	private static class Cache<E> {
		private final long time;
		private final E data;

		Cache(E data) {
			this.data = data;
			time = System.currentTimeMillis();
		}
	}

	public interface CachePredicate<E, ERR extends Exception> {
		boolean test(E data, long time) throws ERR;
	}

	public interface Supplier<T, ERR extends Exception> {
		T get() throws ERR;
	}

	private Cache<E> cache;
	private final CachePredicate<E, ERR> cacheTest;

	public CacheContainer(CachePredicate<E, ERR> cacheTest) {
		this.cacheTest = cacheTest;
	}

	public <ERR2 extends Exception> E orElseGet(Supplier<E, ERR2> get) throws ERR, ERR2 {
		if (cache == null) {
			cache = new Cache<>(get.get());
		} else if (!cacheTest.test(cache.data, cache.time)) {
			cache = new Cache<>(get.get());
		}

		return cache.data;
	}

	public interface CachePredicateForKey<K, E, ERR extends Exception> {
		boolean test(K key, E data, long time) throws ERR;
	}

	public interface CacheContainerMap<K, E, ERR extends Exception> {
		CacheContainer<E, ERR> get(K k);
	}

	public static <K, E, ERR extends Exception> CacheContainerMap<K, E, ERR> createMap(
			CachePredicateForKey<K, E, ERR> cacheTest) {
		Map<K, CacheContainer<E, ERR>> map = new ConcurrentHashMap<>();
		return key -> {
			CacheContainer<E, ERR> c = map.get(key);
			if (c == null) {
				c = new CacheContainer<>((data, time) -> cacheTest.test(key, data, time));
				map.put(key, c);
			}
			return c;
		};
	}

	public static <K, E, ERR extends Exception> CacheContainerMap<K, E, ERR> createMap(
			CachePredicate<E, ERR> cacheTest) {
		return createMap((k, data, time) -> cacheTest.test(data, time));
	}

}
