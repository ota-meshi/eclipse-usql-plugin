package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 */
public class PreferURLClassLoader extends URLClassLoader {
	private final ClassLoader defaultClassLoader;

	/**
	 * Constructor<br>
	 *
	 * classの検索は、URLで行った後defaultClassLoaderを検索します。
	 *
	 * @param defaultClassLoader default ClassLoader
	 * @param urls the URLs from which to load classes and resources
	 */
	public PreferURLClassLoader(ClassLoader defaultClassLoader, URL... urls) {
		super(urls);
		this.defaultClassLoader = defaultClassLoader;
	}

	/**
	 * Constructor
	 *
	 * @param urls the URLs from which to load classes and resources
	 */
	public PreferURLClassLoader(URL... urls) {
		this(Thread.currentThread().getContextClassLoader(), urls);
	}

	/**
	 * 指定クラスをClassLoaderに登録し、ClassLoaderからClassを取得する
	 *
	 * @param <T> 登録する型
	 * @param type 登録するクラス
	 * @return Class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Class<T> defineAndLoadClass(Class<? extends T> type) {
		String name = type.getName();
		try {
			Class<T> c = (Class) loadClass(name);
			if (c.getClassLoader().equals(this)) {
				return c;
			}
		} catch (ClassNotFoundException e) {
			//ignore
		}

		try (InputStream input = type.getClassLoader()
				.getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
				ByteArrayOutputStream output = new ByteArrayOutputStream();) {
			copy(input, output);
			byte[] b = output.toByteArray();
			return (Class) defineClass(name, b, 0, b.length);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static long copy(InputStream source, OutputStream sink)
			throws IOException {
		long nread = 0L;
		byte[] buf = new byte[8192];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException e) {
			//ignore
		}
		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException e) {
			//ignore
		}

		return defaultClassLoader.loadClass(name);
	}

}