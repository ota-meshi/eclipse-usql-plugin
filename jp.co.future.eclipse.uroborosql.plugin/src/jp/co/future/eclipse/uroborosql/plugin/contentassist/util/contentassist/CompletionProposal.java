package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.function.Supplier;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;

public class CompletionProposal implements IPointCompletionProposal {

	public static class DocReplacement {
		private final String replacementString;
		private final int replacementOffset;
		private final int replacementLength;
		private final int cursorPosition;

		public DocReplacement(String replacementString, int replacementOffset, int replacementLength,
				int cursorPosition) {
			Assert.isNotNull(replacementString);
			Assert.isTrue(replacementOffset >= 0);
			Assert.isTrue(replacementLength >= 0);
			Assert.isTrue(cursorPosition >= 0);

			this.replacementString = replacementString;
			this.replacementOffset = replacementOffset;
			this.replacementLength = replacementLength;
			this.cursorPosition = cursorPosition;
		}
	}

	private final int lazyPoint;
	private final String displayString;
	private final String additionalProposalInfo;
	private final Supplier<DocReplacement> replacementSupplier;
	private DocReplacement replacement;

	private DocReplacement getReplacement() {
		return replacement != null ? replacement : (replacement = replacementSupplier.get());
	}

	public CompletionProposal(int lazyPoint, Supplier<DocReplacement> replacementSupplier, String displayString,
			String additionalProposalInfo) {
		this.lazyPoint = lazyPoint;
		this.displayString = displayString;
		this.additionalProposalInfo = additionalProposalInfo;
		this.replacementSupplier = replacementSupplier;
	}

	public CompletionProposal(int lazyPoint, String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, String displayString, String additionalProposalInfo) {
		this(lazyPoint,
				() -> new DocReplacement(replacementString, replacementOffset, replacementLength, cursorPosition),
				displayString, additionalProposalInfo);
	}

	@Override
	public void apply(IDocument document) {
		try {
			DocReplacement replacement = getReplacement();

			//一致している部分を上書き
			int replacementLength = calcReplacementLength(document, replacement);

			document.replace(replacement.replacementOffset, replacementLength, replacement.replacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	private static int calcReplacementLength(IDocument document, DocReplacement replacement) {
		try {
			char[] replacementChars = replacement.replacementString.toCharArray();
			int length = document.getLength();
			int index = 0;
			int offset;
			while (length > (offset = index + replacement.replacementOffset) && replacementChars.length > index) {
				if (replacementChars[index] != document.getChar(offset)) {
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
		return new Point(replacement.replacementOffset + replacement.cursorPosition, 0);
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
}
