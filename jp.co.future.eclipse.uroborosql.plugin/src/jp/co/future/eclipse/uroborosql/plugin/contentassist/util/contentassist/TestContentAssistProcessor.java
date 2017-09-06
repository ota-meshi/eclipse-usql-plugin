package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TestContentAssistProcessor implements IPartContentAssistProcessor {

	private final Function<DocumentPoint, OptionalInt> test;

	private final String displayString;
	private final Supplier<String> additionalProposalInfo;

	private final Supplier<Replacement> replacementSupplier;
	private Replacement replacement;

	private Replacement getReplacement() {
		return replacement != null ? replacement : (replacement = replacementSupplier.get());
	}

	public TestContentAssistProcessor(Function<DocumentPoint, OptionalInt> test,
			Supplier<Replacement> replacementSupplier,
			String displayString, Supplier<String> additionalProposalInfo) {
		this.test = test;
		this.displayString = displayString;
		this.additionalProposalInfo = additionalProposalInfo;
		this.replacementSupplier = replacementSupplier;
	}

	@Override
	public Optional<IPointCompletionProposal> computeCompletionProposal(DocumentPoint point) {
		OptionalInt diffPoint = test.apply(point);
		if (!diffPoint.isPresent()) {
			return Optional.empty();
		}

		return Optional.of(new CompletionProposal(diffPoint.getAsInt(),
				() -> {
					int replacementLength = point.getDocument().getUserOffset() - point.point();
					Replacement replacement = getReplacement();
					return new CompletionProposal.DocReplacement(replacement.replacementStrings,
							point.point(), replacementLength,
							replacement.cursorPosition, replacement.needLinefeed);
				},
				displayString, additionalProposalInfo.get()));
	}
}
