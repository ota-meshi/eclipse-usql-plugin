package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.IBranch;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode.AssistText;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.Branch;

public class TreeContentAssistProcessor implements IListContentAssistProcessor {
	private static class LazyPointNode {
		private final int lazyPoint;
		private final INamedNode node;

		LazyPointNode(int lazyPoint, INamedNode node) {
			this.lazyPoint = lazyPoint;
			this.node = node;
		}
	}

	private final Branch root = IBranch.ofUnknown("root");

	public static Collection<LazyPointNode> getAssists(INamedNode node, String name) {
		List<LazyPointNode> result = new ArrayList<>();

		node.children().distinct().forEach(n -> {
			OptionalInt point = HitTester.hit(name, n.name());
			if (!point.isPresent()) {
				return;
			}
			result.add(new LazyPointNode(point.getAsInt(), n));
		});

		return result;
	}

	public void addAll(Collection<? extends INamedNode> nodes) {
		nodes.forEach(this::add);
	}

	public void add(INamedNode node) {
		root.putChild(node);
	}

	@Override
	public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint point) {
		INamedNode node = root;
		List<String> userProps = parseProps(point.getRangeText());
		List<String> replacementProps = new ArrayList<>();

		for (int i = 0; i < userProps.size() - 1; i++) {
			String prop = userProps.get(i);
			INamedNode n = node.getTokenChild(prop);
			if (n == null) {
				return Collections.emptyList();
			}
			replacementProps.add(prop);
			node = n;
		}
		String endPropName = userProps.get(userProps.size() - 1);
		Collection<LazyPointNode> hits = getAssists(node, endPropName);
		int replacementLength = point.getDocument().getUserOffset() - point.point();

		return hits.stream()
				.filter(p -> !endPropName.equals(p.node.name()))
				.map(p -> {
					AssistText assistText = p.node.createAssistText();
					String replacementString = String.join(".", replacementProps);
					int cursorPosition;
					if (replacementString.isEmpty()) {
						cursorPosition = assistText.getCursorPosition();
						replacementString = assistText.getReplacementString();
					} else {
						cursorPosition = replacementString.length() + 1 + assistText.getCursorPosition();
						replacementString += "." + assistText.getReplacementString();
					}
					return new CompletionProposal(p.lazyPoint, new String[] { replacementString }, point.point(),
							replacementLength,
							cursorPosition, assistText.getCursorLength(), false, p.node.toDisplayString(),
							Objects.toString(p.node.additionalProposalInfo(), ""));
				})
				.collect(Collectors.toList());

	}

	private List<String> parseProps(String text) {
		if (text.isEmpty()) {
			return Arrays.asList("");
		}
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		PrimitiveIterator.OfInt itr = text.codePoints().iterator();
		while (itr.hasNext()) {
			int c = itr.nextInt();

			if (c == '(') {
				sb.append(Character.toChars(c));
				skipPair(itr, c, sb);
				continue;
			} else if (c == '.') {
				result.add(sb.toString());
				sb.setLength(0);
			} else {
				sb.append(Character.toChars(c));
			}
		}
		result.add(sb.toString());
		return result;
	}

	private void skipPair(PrimitiveIterator.OfInt itr, int ch, StringBuilder sb) {
		if (ch == '(') {
			while (itr.hasNext()) {
				int c = itr.nextInt();
				sb.append(Character.toChars(c));
				if (c == '(') {
					skipPair(itr, ch, sb);
				} else if (c == ')') {
					return;
				}
			}
		}
	}

}
