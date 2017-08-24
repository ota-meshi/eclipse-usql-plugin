package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

class ParentElementTarget implements ITarget {
	private final Element parent;
	private final String name;

	ParentElementTarget(Element parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Optional<AttributableValue> get() {
		//get attr
		return Xml.asMap(parent.getAttributes()).entrySet().stream()
				.filter(e -> e.getKey().equalsIgnoreCase(name))
				.findFirst()
				.map(e -> e.getValue())
				.map(AttributableValue::new);
	}

	@Override
	public List<AttributableValue> list(String pluralName) {
		Optional<Element> leaf = Xml.children(parent).stream()
				.filter(e -> e.getTagName().equalsIgnoreCase(pluralName))
				.findFirst();
		if (leaf.isPresent()) {
			return AttributableValue.getValues(leaf.get());
		}
		Optional<String> attr = Xml.asMap(parent.getAttributes()).entrySet().stream()
				.filter(e -> e.getKey().equalsIgnoreCase(pluralName))
				.findFirst()
				.map(e -> e.getValue());

		if (attr.isPresent()) {
			return Arrays.stream(attr.get().split(","))
					.map(String::trim)
					.map(AttributableValue::new)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
