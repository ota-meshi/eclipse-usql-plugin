package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

public interface ITarget {
	static final ITarget EMPTY = new ITarget() {

		@Override
		public Optional<AttributableValue> get() {
			return Optional.empty();
		}

		@Override
		public List<AttributableValue> list(String pluralName) {
			return Collections.emptyList();
		}

		@Override
		public String name() {
			return "empty";
		}

	};

	String name();

	Optional<AttributableValue> get();

	default Optional<String> value() {
		return get().map(AttributableValue::value);
	}

	default List<AttributableValue> list() {
		return list(name() + "s");
	}

	default List<String> values() {
		return values(name() + "s");
	}

	List<AttributableValue> list(String pluralName);

	default List<String> values(String pluralName) {
		List<AttributableValue> list = list(pluralName);
		return new AbstractList<String>() {
			@Override
			public String get(int index) {
				return list.get(index).value();
			}

			@Override
			public int size() {
				return list.size();
			}

		};
	}

	public static ITarget get(Element root, String... names) {
		Deque<String> nameList = new LinkedList<>(Arrays.asList(names));
		if (nameList.isEmpty()) {
			return EMPTY;
		}

		String leafName = nameList.pollLast();
		Element parent = root;
		if (!nameList.isEmpty()) {
			String name = nameList.poll();

			Iterator<Element> children = Xml.children(parent).iterator();

			while (children.hasNext()) {
				Element element = children.next();
				if (element.getTagName().equalsIgnoreCase(name)) {
					name = nameList.poll();
					parent = element;
					children = Xml.children(parent).iterator();
				}
			}
		}
		if (nameList.isEmpty()) {
			List<Element> leafs = Xml.children(parent).stream()
					.filter(e -> e.getTagName().equalsIgnoreCase(leafName))
					.collect(Collectors.toList());
			if (!leafs.isEmpty()) {
				return new ElementsTarget(leafs);
			}
			return new ParentElementTarget(parent, leafName);
		}
		return EMPTY;
	}

}