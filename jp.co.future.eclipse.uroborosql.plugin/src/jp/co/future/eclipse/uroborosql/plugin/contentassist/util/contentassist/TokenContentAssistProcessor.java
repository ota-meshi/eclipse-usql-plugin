package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TokenContentAssistProcessor extends TestContentAssistProcessor {
	public TokenContentAssistProcessor(String text, Supplier<String> additionalProposalInfo) {
		this(text, () -> new Replacement(text), text, additionalProposalInfo);
	}

	public TokenContentAssistProcessor(String test, String displayAndReplacementString,
			Supplier<String> additionalProposalInfo) {
		super(toHitTest(test), () -> new Replacement(displayAndReplacementString),
				displayAndReplacementString,
				additionalProposalInfo);
	}

	public TokenContentAssistProcessor(String test, Supplier<Replacement> replacementSupplier, String displayString,
			Supplier<String> additionalProposalInfo) {
		super(toHitTest(test), replacementSupplier, displayString, additionalProposalInfo);
	}

	private static Function<DocumentPoint, OptionalInt> toHitTest(String test) {
		return p -> HitTester.hitTokens(p.getRangeText(), test);
	}
}
