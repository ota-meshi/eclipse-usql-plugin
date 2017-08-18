package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.function.Predicate;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class FmtContentAssistProcessor extends TestContentAssistProcessor {
	public FmtContentAssistProcessor(String text, String additionalProposalInfo) {
		this(text, text, text, additionalProposalInfo);
	}

	public FmtContentAssistProcessor(String text, String replacementString, String displayString,
			String additionalProposalInfo) {
		this(text, replacementString, replacementString.length(), displayString, additionalProposalInfo);
	}

	public FmtContentAssistProcessor(String test, String replacementString, int cursorPosition, String displayString,
			String additionalProposalInfo) {
		super(toHitTest(test), replacementString, cursorPosition, displayString, additionalProposalInfo);
	}

	private static Predicate<DocumentPoint> toHitTest(String test) {
		String s = remSpace(test);
		return p -> s.startsWith(remSpace(p.getRangeText()));
	}

	private static String remSpace(String s) {
		int[] codePoints = s.codePoints().filter(c -> !Character.isWhitespace(c)).toArray();
		return new String(codePoints, 0, codePoints.length);
	}
}
