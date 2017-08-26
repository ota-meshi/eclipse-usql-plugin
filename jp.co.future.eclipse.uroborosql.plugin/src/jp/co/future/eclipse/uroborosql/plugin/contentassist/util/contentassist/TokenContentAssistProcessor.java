package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TokenContentAssistProcessor extends TestContentAssistProcessor {
	public TokenContentAssistProcessor(String text, String additionalProposalInfo) {
		this(text, text, text, additionalProposalInfo);
	}

	public TokenContentAssistProcessor(String text, String replacementString, String displayString,
			String additionalProposalInfo) {
		this(text, replacementString, replacementString.length(), displayString, additionalProposalInfo);
	}

	public TokenContentAssistProcessor(String test, String replacementString, int cursorPosition, String displayString,
			String additionalProposalInfo) {
		super(toHitTest(test), replacementString, cursorPosition, displayString, additionalProposalInfo);
	}

	private static Function<DocumentPoint, OptionalInt> toHitTest(String test) {
		return p -> HitTester.hitTokens(p.getRangeText(), test);
	}
}
