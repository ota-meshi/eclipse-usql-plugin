package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Xml {
	private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);//dtd読み込みをしない
		return builderFactory.newDocumentBuilder();
	}

	public static Document parse(InputStream input) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = newDocumentBuilder();
		return builder.parse(input);
	}

	public static Document parse(String string) throws ParserConfigurationException, SAXException, IOException {
		return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	public static List<Element> children(Element parent) {
		List<Node> nodeList = asList(parent.getChildNodes());

		return nodeList.stream()
				.filter(Element.class::isInstance)
				.map(Element.class::cast)
				.collect(Collectors.toList());
	}

	public static List<Node> asList(NodeList nodeList) {
		return new AbstractList<Node>() {
			@Override
			public Node get(int index) {
				return nodeList.item(index);
			}

			@Override
			public int size() {
				return nodeList.getLength();
			}
		};
	}

	public static Map<String, String> asMap(NamedNodeMap attributes) {
		List<Node> list = new AbstractList<Node>() {
			@Override
			public Node get(int index) {
				return attributes.item(index);
			}

			@Override
			public int size() {
				return attributes.getLength();
			}
		};
		Set<Map.Entry<String, String>> entrySet = new AbstractSet<Map.Entry<String, String>>() {

			@Override
			public Iterator<Entry<String, String>> iterator() {
				return list.stream()
						.filter(Attr.class::isInstance)
						.map(Attr.class::cast)
						.map(a -> (Entry<String, String>) new AbstractMap.SimpleEntry<>(a.getName(), a.getValue()))
						.iterator();
			}

			@Override
			public int size() {
				return list.size();
			}
		};

		return new AbstractMap<String, String>() {

			@Override
			public Set<Entry<String, String>> entrySet() {
				return entrySet;
			}

		};
	}
}
