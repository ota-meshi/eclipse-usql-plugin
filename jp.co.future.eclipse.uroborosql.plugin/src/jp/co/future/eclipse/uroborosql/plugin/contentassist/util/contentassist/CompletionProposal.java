package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.StringJoiner;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.utils.Characters;
import jp.co.future.eclipse.uroborosql.plugin.utils.Eclipses;

public class CompletionProposal implements IPointCompletionProposal {

	public static class DocReplacement {
		private final String[] replacementStrings;
		private final int replacementOffset;
		private final int replacementLength;
		private final OptionalInt cursorPosition;
		private final boolean needLinefeed;

		public DocReplacement(String replacementString, int replacementOffset, int replacementLength,
				OptionalInt cursorPosition, boolean needLinefeed) {
			this(new String[] { replacementString }, replacementOffset, replacementLength, cursorPosition,
					needLinefeed);
		}

		public DocReplacement(String[] replacementStrings, int replacementOffset, int replacementLength,
				OptionalInt cursorPosition, boolean needLinefeed) {
			Assert.isNotNull(replacementStrings);
			Assert.isTrue(replacementOffset >= 0);
			Assert.isTrue(replacementLength >= 0);

			this.replacementStrings = replacementStrings;
			this.replacementOffset = replacementOffset;
			this.replacementLength = replacementLength;
			this.cursorPosition = cursorPosition;
			this.needLinefeed = needLinefeed;
		}
	}

	private final int lazyPoint;
	private final String displayString;
	private final String additionalProposalInfo;
	private final Supplier<DocReplacement> replacementSupplier;
	private DocReplacement replacement;
	private IndentReplacement indentReplacement;

	private DocReplacement getReplacement() {
		return replacement != null ? replacement : (replacement = replacementSupplier.get());
	}

	public CompletionProposal(DocReplacement replacement, String displayString,
			String additionalProposalInfo) {
		lazyPoint = 0;
		this.displayString = displayString;
		this.additionalProposalInfo = additionalProposalInfo;
		replacementSupplier = () -> replacement;
	}

	public CompletionProposal(int lazyPoint, Supplier<DocReplacement> replacementSupplier, String displayString,
			String additionalProposalInfo) {
		this.lazyPoint = lazyPoint;
		this.displayString = displayString;
		this.additionalProposalInfo = additionalProposalInfo;
		this.replacementSupplier = replacementSupplier;
	}

	public CompletionProposal(int lazyPoint, String[] replacementStrings, int replacementOffset, int replacementLength,
			int cursorPosition, boolean needLinefeed, String displayString, String additionalProposalInfo) {
		this(lazyPoint,
				() -> new DocReplacement(replacementStrings, replacementOffset, replacementLength,
						OptionalInt.of(cursorPosition),
						needLinefeed),
				displayString, additionalProposalInfo);
	}

	@Override
	public void apply(IDocument document) {
		try {
			DocReplacement replacement = getReplacement();

			IndentReplacement indentReplacement = getIndentReplacement(document);

			//一致している部分を上書き
			int replacementLength = calcReplacementLength(document, replacement, indentReplacement.replacementString);

			String replacementString = indentReplacement.replacementString;
			if (replacement.needLinefeed) {
				int endOffset = replacement.replacementOffset + replacementLength;
				if (endOffset < document.getLength()) {
					char c = document.getChar(endOffset);
					if (c != '\n' && c != '\r') {
						replacementString += "\n";
					}
				}
			}

			document.replace(replacement.replacementOffset, replacementLength, replacementString);

		} catch (BadLocationException x) {
			// ignore
		}
	}

	private static int calcReplacementLength(IDocument document, DocReplacement replacement, String replacementString) {
		try {
			char[] replacementChars = replacementString.toCharArray();
			int length = document.getLength();
			int index = 0;
			int offset;
			while (length > (offset = index + replacement.replacementOffset) && replacementChars.length > index) {
				if (!Characters.equalsIgnoreCase(replacementChars[index], document.getChar(offset))) {
					return Math.max(replacement.replacementLength, index);
				}
				index++;

			}
			return Math.max(replacement.replacementLength, index);
		} catch (BadLocationException e) {
			return replacement.replacementLength;
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		DocReplacement replacement = getReplacement();
		int cursorPosition;
		try {
			IndentReplacement indentReplacement = getIndentReplacement(document);
			cursorPosition = indentReplacement.cursorPosition;
		} catch (BadLocationException e) {
			cursorPosition = replacement.cursorPosition.orElse(0);
		}

		return new Point(replacement.replacementOffset + cursorPosition, 0);
	}

	@Override
	public Image getImage() {
		return UroboroSQLUtils.getImage();
	}

	@Override
	public String getDisplayString() {
		return displayString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return additionalProposalInfo
				+ "<br>"
				+ "<br>"
				+ "- uroboroSQL -";
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public int getLazyPoint() {
		return lazyPoint;
	}

	private static class IndentReplacement {
		private final String replacementString;
		private final int cursorPosition;

		IndentReplacement(String replacementString, OptionalInt cursorPosition) {
			this.replacementString = replacementString;
			this.cursorPosition = cursorPosition.orElse(replacementString.length());
		}
	}

	private IndentReplacement getIndentReplacement(IDocument document) throws BadLocationException {
		if (indentReplacement != null) {
			return indentReplacement;
		}
		DocReplacement replacement = getReplacement();
		return indentReplacement = buildIndentReplacement(document, replacement);
	}

	private static IndentReplacement buildIndentReplacement(IDocument document, DocReplacement replacement)
			throws BadLocationException {

		if (replacement.replacementStrings.length == 1) {
			return new IndentReplacement(replacement.replacementStrings[0], replacement.cursorPosition);
		}

		int cursorPosition = replacement.cursorPosition.orElse(0);
		int linesTotalLength = 0;
		String indent = getIndent(document, replacement.replacementOffset);
		String lf = Eclipses.getLineDelimiter(document);
		int lineAdditionalOffset = indent.length() + lf.length() - 1;
		StringJoiner joiner = new StringJoiner(lf + indent);
		for (String replacementLine : replacement.replacementStrings) {
			linesTotalLength += replacementLine.length() + 1;
			if (replacement.cursorPosition.isPresent()) {
				if (linesTotalLength <= replacement.cursorPosition.getAsInt()) {
					cursorPosition += lineAdditionalOffset;
				}
			}
			joiner.add(replacementLine);
		}
		return new IndentReplacement(joiner.toString(),
				replacement.cursorPosition.isPresent() ? OptionalInt.of(cursorPosition) : OptionalInt.empty());
	}

	private static String getIndent(IDocument document, int replacementOffset) throws BadLocationException {
		for (int i = replacementOffset; i >= 0; i--) {
			char c = document.getChar(i);
			if (c == '\n' || c == '\r') {
				return buildIndent(document, i + 1);
			}

		}
		return buildIndent(document, 0);
	}

	private static String buildIndent(IDocument document, int lineStart) throws BadLocationException {
		char c = document.getChar(lineStart);
		if (!Character.isWhitespace(c)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = lineStart; i < document.getLength(); i++) {
			c = document.getChar(i);
			if (!Character.isWhitespace(c)) {
				return builder.toString();
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
