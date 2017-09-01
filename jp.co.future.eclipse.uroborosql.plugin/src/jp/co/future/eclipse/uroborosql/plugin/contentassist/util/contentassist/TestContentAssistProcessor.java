package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.StringJoiner;
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
					IndentReplacement indentReplacement = buildIndentReplacement(point, getReplacement());
					return new CompletionProposal.DocReplacement(indentReplacement.getReplacementString(),
							point.point(), replacementLength,
							indentReplacement.getCursorPosition());
				},
				displayString, additionalProposalInfo.get()));
	}

	private static class IndentReplacement {
		private final String replacementString;
		private final int cursorPosition;

		IndentReplacement(String replacementString, int cursorPosition) {
			this.replacementString = replacementString;
			this.cursorPosition = cursorPosition;
		}

		public String getReplacementString() {
			return replacementString;
		}

		public int getCursorPosition() {
			return cursorPosition;
		}
	}

	private static IndentReplacement buildIndentReplacement(DocumentPoint point, Replacement replacement) {
		if (replacement.replacementStrings.length == 1) {
			return new IndentReplacement(replacement.replacementStrings[0], replacement.cursorPosition);
		}

		int cursorPosition = replacement.cursorPosition;
		int linesTotalLength = 0;
		String indent = point.getIndent();
		StringJoiner joiner = new StringJoiner("\n" + indent);
		for (String replacementLine : replacement.replacementStrings) {
			linesTotalLength += replacementLine.length() + 1/*CR*/;
			if (linesTotalLength <= replacement.cursorPosition) {
				cursorPosition += indent.length();
			}
			joiner.add(replacementLine);
		}
		return new IndentReplacement(joiner.toString(), cursorPosition);
	}

}
