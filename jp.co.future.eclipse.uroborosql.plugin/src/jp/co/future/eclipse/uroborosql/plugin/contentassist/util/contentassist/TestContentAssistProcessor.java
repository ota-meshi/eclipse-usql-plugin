package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TestContentAssistProcessor implements PartContentAssistProcessor {
	private final Predicate<DocumentPoint> test;
	private final String[] replacementLines;;
	private final int cursorPosition;
	private final String displayString;
	private final String additionalProposalInfo;

	public TestContentAssistProcessor(Predicate<DocumentPoint> test, String replacementString, int cursorPosition,
			String displayString, String additionalProposalInfo) {
		this(test, new String[] { replacementString }, cursorPosition, displayString, additionalProposalInfo);
	}

	public TestContentAssistProcessor(Predicate<DocumentPoint> test, String[] replacementLines, int cursorPosition,
			String displayString, String additionalProposalInfo) {
		this.test = test;
		this.replacementLines = replacementLines;
		this.cursorPosition = cursorPosition;
		this.displayString = displayString;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public Optional<ICompletionProposal> computeCompletionProposal(DocumentPoint point) {
		if (!test.test(point)) {
			return Optional.empty();
		}

		int replacementLength = point.getDocument().getUserOffset() - point.point();

		IReplacement replacement = buildReplacement(point);

		return Optional.of(new CompletionProposal(replacement.getReplacementString(), point.point(), replacementLength,
				replacement.getCursorPosition(),
				UroboroSQLUtils.getImage(),
				displayString,
				/* contextInformation */null,
				additionalProposalInfo));
	}

	private interface IReplacement {
		String getReplacementString();

		int getCursorPosition();
	}

	private final IReplacement singleReplacement = new IReplacement() {

		@Override
		public String getReplacementString() {
			return replacementLines[0];
		}

		@Override
		public int getCursorPosition() {
			return cursorPosition;
		}
	};

	private static class Replacement implements IReplacement {
		private final String replacementString;
		private final int cursorPosition;

		Replacement(String replacementString, int cursorPosition) {
			this.replacementString = replacementString;
			this.cursorPosition = cursorPosition;
		}

		@Override
		public String getReplacementString() {
			return replacementString;
		}

		@Override
		public int getCursorPosition() {
			return cursorPosition;
		}
	}

	private IReplacement buildReplacement(DocumentPoint point) {
		if (replacementLines.length == 1) {
			return singleReplacement;
		}

		int cursorPosition = this.cursorPosition;
		int linesTotalLength = 0;
		String indent = point.getIndent();
		StringJoiner joiner = new StringJoiner("\n" + indent);
		for (String replacementLine : replacementLines) {
			linesTotalLength += replacementLine.length() + 1/*CR*/;
			if (linesTotalLength <= this.cursorPosition) {
				cursorPosition += indent.length();
			}
			joiner.add(replacementLine);
		}
		return new Replacement(joiner.toString(), cursorPosition);
	}

}
