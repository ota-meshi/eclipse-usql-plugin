package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TextContentAssistProcessor extends TestContentAssistProcessor {

	public TextContentAssistProcessor(String text, boolean needLinefeed, String displayString,
			Supplier<String> additionalProposalInfo) {
		this(text, new Replacement(text, needLinefeed), displayString, additionalProposalInfo);
	}

	public TextContentAssistProcessor(String test, Replacement replacement,
			String displayString,
			Supplier<String> additionalProposalInfo) {
		this(test, () -> replacement, displayString, additionalProposalInfo);
	}

	public TextContentAssistProcessor(String test, Supplier<Replacement> replacementSupplier,
			String displayString,
			Supplier<String> additionalProposalInfo) {
		super(toHitTest(test), replacementSupplier, displayString, additionalProposalInfo);
	}

	private static Function<DocumentPoint, OptionalInt> toHitTest(String test) {
		return p -> HitTester.hit(p.getRangeText(), test);
	}
}
