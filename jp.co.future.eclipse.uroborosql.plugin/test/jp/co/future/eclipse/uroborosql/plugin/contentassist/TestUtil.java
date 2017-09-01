package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

public class TestUtil {

	public static ITextViewer createTextViewer(String text) {
		IDocument doc = createDocument(text);
		return (ITextViewer) Proxy.newProxyInstance(TestUtil.class.getClassLoader(),
				new Class<?>[] { ITextViewer.class }, (proxy, method, args) -> {
					if (method.getName().equals("getDocument") && method.getParameterCount() == 0) {
						return doc;
					}
					throw new IllegalStateException("未対応:" + method.toString());
				});
	}

	public static IDocument createDocument(String text) {
		class InvocationHandlerImpl implements InvocationHandler {
			private String text;

			InvocationHandlerImpl(String text) {
				this.text = text;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("get") && method.getParameterCount() == 0) {
					return text;
				}
				if (method.getName().equals("get")
						&& Arrays.equals(method.getParameterTypes(), new Class<?>[] { int.class, int.class })) {
					int offset = (Integer) args[0];
					int length = (Integer) args[1];
					return text.substring(offset, offset + length);
				}
				if (method.getName().equals("getLength") && method.getParameterCount() == 0) {
					return text.length();
				}
				if (method.getName().equals("getChar")
						&& Arrays.equals(method.getParameterTypes(), new Class<?>[] { int.class })) {
					int offset = (Integer) args[0];
					return text.charAt(offset);
				}
				if (method.getName().equals("replace")
						&& Arrays.equals(method.getParameterTypes(),
								new Class<?>[] { int.class, int.class, String.class })) {
					int offset = (Integer) args[0];
					int length = (Integer) args[1];
					String reptext = (String) args[2];

					StringBuilder builder = new StringBuilder(text);
					builder.delete(offset, offset + length);
					builder.insert(offset, reptext);

					text = builder.toString();

					return null;
				}

				if (method.getName().equals("toString") && method.getParameterCount() == 0) {
					return escape(text);
				}

				throw new IllegalStateException("未対応:" + method.toString());
			}

		}

		return (IDocument) Proxy.newProxyInstance(TestUtil.class.getClassLoader(), new Class<?>[] { IDocument.class },
				new InvocationHandlerImpl(text));
	}

	public static abstract class PrintList<E> extends AbstractList<E> {
		private final List<E> list = new ArrayList<>();
		private boolean printLines = true;

		@Override
		public void add(int index, E element) {
			list.add(index, element);
		}

		@Override
		public E get(int index) {
			return list.get(index);
		}

		@Override
		public int size() {
			return list.size();
		}

		protected abstract String toString(E e);

		@Override
		public String toString() {
			if (this.printLines) {
				return stream()
						.map(this::toString)
						.collect(Collectors.joining(",\n", "Arrays.asList(\n", "\n)"));
			} else {
				return stream()
						.map(this::toString)
						.collect(Collectors.joining(", ", "Arrays.asList(\n", "\n)"));
			}
		}

		public PrintList<E> setPrintLines(boolean printLines) {
			this.printLines = printLines;
			return this;
		}

	}

	public static abstract class PrintMap<K, V> extends AbstractMap<K, V> {
		private final Map<K, V> map = new HashMap<>();
		private boolean printLines = true;

		public PrintMap() {
		}

		public PrintMap(Map<K, V> map) {
			this.map.putAll(map);
		}

		@Override
		public V put(K key, V value) {
			return this.map.put(key, value);
		}

		protected abstract String[] toString(K k, V v);

		@Override
		public Set<Entry<K, V>> entrySet() {
			return map.entrySet();
		}

		@Override
		public String toString() {
			if (this.printLines) {
				return map.entrySet().stream()
						.map(e -> this.toString(e.getKey(), e.getValue()))
						.map(s -> s[0] + ", " + s[1])
						.collect(Collectors.joining(")\n.p(", "new M<>(", ")\n"));
			} else {
				return map.entrySet().stream()
						.map(e -> this.toString(e.getKey(), e.getValue()))
						.map(s -> s[0] + ", " + s[1])
						.collect(Collectors.joining(").p(", "new M<>(", ")\n"));
			}
		}

		public PrintMap<K, V> setPrintLines(boolean printLines) {
			this.printLines = printLines;
			return this;
		}

	}

	public static class M<K, V> extends PrintMap<K, V> {

		public M(K k, V v) {
			put(k, v);
		}

		@Override
		protected String[] toString(K k, V v) {
			return new String[] { stringLiteral(k), stringLiteral(v) };
		}

		public M<K, V> p(K k, V v) {
			put(k, v);
			return this;
		}
	}

	public static class StringList extends PrintList<String> {

		public StringList(List<String> ss) {
			addAll(ss);
		}

		public StringList(String... ss) {
			Collections.addAll(this, ss);
		}

		public StringList() {
		}

		@Override
		protected String toString(String e) {
			return stringLiteral(e);
		}

	}

	public static class StringMap extends PrintMap<String, String> {

		@Override
		protected String[] toString(String k, String v) {
			return new String[] { stringLiteral(k), stringLiteral(v) };
		}

	}

	public static String stringLiteral(Object o) {
		if (o instanceof String) {
			return "\"" + escape((String) o) + "\"";
		}
		if (o == null) {
			return "null";
		}
		return "\"" + o.toString() + "\"";
	}

	public static String escape(String s) {
		return s.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t")
				.replace("\"", "\\\"");
	}
}
