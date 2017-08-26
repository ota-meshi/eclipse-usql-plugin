package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TextContentAssistProcessor extends TestContentAssistProcessor {

	public TextContentAssistProcessor(String text, String displayString, String additionalProposalInfo) {
		this(text, text, text.length(), displayString, additionalProposalInfo);
	}

	public TextContentAssistProcessor(String test, String replacementString, int cursorPosition, String displayString,
			String additionalProposalInfo) {
		super(toHitTest(test), replacementString, cursorPosition, displayString, additionalProposalInfo);
	}

	public TextContentAssistProcessor(String test, String[] replacementLines, int cursorPosition, String displayString,
			String additionalProposalInfo) {
		super(toHitTest(test), replacementLines, cursorPosition, displayString, additionalProposalInfo);
	}

	private static Function<DocumentPoint, OptionalInt> toHitTest(String test) {
		return p -> HitTester.hit(p.getRangeText(), test);
	}
}
