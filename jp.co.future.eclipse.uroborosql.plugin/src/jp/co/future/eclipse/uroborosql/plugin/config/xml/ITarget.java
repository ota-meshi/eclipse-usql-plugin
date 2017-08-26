package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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

	static ITarget get(Element root, String... names) {
		Deque<String> nameList = new LinkedList<>(Arrays.asList(names));
		if (nameList.isEmpty()) {
			return EMPTY;
		}

		String leafName = nameList.pollLast();
		Element element = _getElementChain(root, nameList);
		if (element == null) {
			return EMPTY;
		}

		List<Element> leafs = Xml.children(element).stream()
				.filter(e -> e.getTagName().equalsIgnoreCase(leafName))
				.collect(Collectors.toList());
		if (!leafs.isEmpty()) {
			return new ElementsTarget(leafs);
		}
		return new ParentElementTarget(element, leafName);
	}

	/**
	 * internal
	 */
	@Deprecated
	static Element _getElementChain(Element element, Deque<String> names) {
		if (names.isEmpty()) {
			return element;
		}
		String name = names.poll();
		for (Element child : Xml.children(element)) {
			if (child.getTagName().equalsIgnoreCase(name)) {
				return _getElementChain(child, names);
			}
		}
		return null;
	}

}