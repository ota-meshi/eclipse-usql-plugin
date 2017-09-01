package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class LazySearchContentAssistProcessor extends TestContentAssistProcessor {
	private final TokenContentAssistProcessor noLazy;

	public LazySearchContentAssistProcessor(String noLazy, Collection<String> testTexts,
			String displayAndReplacementString,
			Supplier<String> additionalProposalInfo) {
		this(noLazy, testTexts, () -> new Replacement(displayAndReplacementString), displayAndReplacementString,
				additionalProposalInfo);
	}

	public LazySearchContentAssistProcessor(String noLazy, Collection<String> testTexts,
			Supplier<Replacement> replacementSupplier,
			String displayString,
			Supplier<String> additionalProposalInfo) {
		super(toHitTest(testTexts), replacementSupplier, "(?)" + displayString, additionalProposalInfo);
		this.noLazy = new TokenContentAssistProcessor(noLazy, replacementSupplier, displayString,
				additionalProposalInfo);
	}

	private static Function<DocumentPoint, OptionalInt> toHitTest(Collection<String> testTexts) {
		return p -> HitTester.search(p.getRangeText(), testTexts);
	}

	@Override
	public Optional<IPointCompletionProposal> computeCompletionProposal(DocumentPoint point) {
		Optional<IPointCompletionProposal> r = noLazy.computeCompletionProposal(point);
		if (r.isPresent()) {
			return r;
		}
		return super.computeCompletionProposal(point);
	}
}
