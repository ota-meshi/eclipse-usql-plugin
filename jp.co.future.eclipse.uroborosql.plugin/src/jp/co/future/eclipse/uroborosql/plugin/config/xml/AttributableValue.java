package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class AttributableValue {

	private final String value;
	private final Map<String, String> attr;

	public AttributableValue(String value) {
		this(value, Collections.emptyMap());
	}

	public AttributableValue(String value, Map<String, String> attr) {
		this.value = value;
		this.attr = attr;
	}

	public static AttributableValue of(Element element) {
		Map<String, String> attr = new HashMap<>(Xml.asMap(element.getAttributes()));
		if (attr.containsKey("value")) {
			String value = Objects.toString(attr.get("value"), "");
			attr.remove("value");
			return new AttributableValue(value, attr);
		} else {
			StringBuilder sb = new StringBuilder();
			for (Node node : Xml.asList(element.getChildNodes())) {
				if (node instanceof Text) {
					sb.append(node.getTextContent());
				}
			}
			String value = sb.toString();
			return new AttributableValue(value, attr);
		}
	}

	public static List<AttributableValue> getValues(Element element) {
		List<Element> values = Xml.children(element).stream()
				.filter(e -> e.getTagName().equalsIgnoreCase("value"))
				.collect(Collectors.toList());
		if (!values.isEmpty()) {
			return values.stream()
					.map(AttributableValue::of)
					.collect(Collectors.toList());
		}

		AttributableValue value = of(element);
		return Arrays.stream(value.value().split(","))
				.map(s -> s.split("\n"))
				.flatMap(Arrays::stream)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> new AttributableValue(s, value.attr()))
				.collect(Collectors.toList());
	}

	public String value() {
		return value;
	}

	public Map<String, String> attr() {
		return attr;
	}
}
