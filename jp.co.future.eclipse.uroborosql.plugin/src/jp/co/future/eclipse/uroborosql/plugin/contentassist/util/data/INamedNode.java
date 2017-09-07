package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.AbstractNamedNode;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentIterable;

public interface INamedNode {
	public class AssistText {
		private final String replacementString;
		private final int cursorPosition;
		private final int cursorLength;

		public AssistText(String replacementString, int cursorPosition, int cursorLength) {
			this.replacementString = replacementString;
			this.cursorPosition = cursorPosition;
			this.cursorLength = cursorLength;
		}

		public String getReplacementString() {
			return replacementString;
		}

		public int getCursorPosition() {
			return cursorPosition;
		}

		public int getCursorLength() {
			return cursorLength;
		}

	}

	NodeLevel nodeLevel();

	String name();

	String additionalProposalInfo();

	AssistText createAssistText();

	FluentIterable<INamedNode> children();

	INamedNode getTokenChild(String token);

	boolean isMatchToken(String token);

	void marge(INamedNode node);

	String toDisplayString();

	static AbstractNamedNode<?> ofUnknownToken(String token) {
		Pattern pattern = Pattern.compile("^(.*)\\((.*)\\)$");
		Matcher matcher = pattern.matcher(token);
		if (matcher.matches()) {
			return IMethod.ofUnknown(matcher.group(1), matcher.group(2).split(","));
		} else {
			return IBranch.ofUnknown(token);
		}
	}

}
