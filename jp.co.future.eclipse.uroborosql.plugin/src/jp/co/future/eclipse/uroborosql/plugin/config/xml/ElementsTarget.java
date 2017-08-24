package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

class ElementsTarget implements ITarget {
	private final List<Element> elements;

	ElementsTarget(List<Element> elements) {
		this.elements = elements;
	}

	@Override
	public String name() {
		return elements.stream().findFirst().map(Element::getTagName).orElse("");
	}

	@Override
	public Optional<AttributableValue> get() {
		return elements.stream()
				.map(AttributableValue::of)
				.findFirst();
	}

	@Override
	public List<AttributableValue> list(String pluralName) {
		return elements.stream()
				.map(AttributableValue::of)
				.collect(Collectors.toList());
	}

}
