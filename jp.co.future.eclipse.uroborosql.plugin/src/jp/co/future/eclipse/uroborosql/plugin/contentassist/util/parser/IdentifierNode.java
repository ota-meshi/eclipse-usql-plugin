package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentifierNode {
	private final IdentifierNode parent;
	private final Token node;
	private final Map<String, IdentifierNode> tree = new HashMap<>();

	public IdentifierNode(IdentifierNode parent, Token node) {
		this.parent = parent;
		this.node = node;
	}

	public IdentifierNode add(Token node) {
		return tree.computeIfAbsent(node.getString(), k -> new IdentifierNode(this, node));
	}

	public IdentifierNode getParent() {
		return parent;
	}

	public String getNode() {
		return node.getString();
	}

	public Collection<IdentifierNode> children() {
		return tree.values();
	}

	@Override
	public String toString() {
		return node + "[" + tree.values().stream()
				.map(IdentifierNode::getNode)
				.collect(Collectors.joining(", ")) + "]";
	}
}
