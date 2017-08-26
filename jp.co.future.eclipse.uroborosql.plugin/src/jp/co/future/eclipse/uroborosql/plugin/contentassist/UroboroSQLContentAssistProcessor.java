package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type.MCommentTypes;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;

public class UroboroSQLContentAssistProcessor implements IContentAssistProcessor {
	private final IContentAssistProcessor original;

	public UroboroSQLContentAssistProcessor() {
		this(null);
	}

	public UroboroSQLContentAssistProcessor(IContentAssistProcessor original) {
		this.original = original;
	}

	static int compareCharCustom(int c1, int c2) {
		int isStandaed = Boolean.compare(Character.isAlphabetic(c1) || Character.isDigit(c1),
				Character.isAlphabetic(c2) || Character.isDigit(c2));
		if (isStandaed != 0) {
			return -isStandaed;
		}
		int isLowerCase = Boolean.compare(Character.isLowerCase(c1) || Character.isDigit(c1),
				Character.isLowerCase(c2) || Character.isDigit(c2));
		if (isLowerCase != 0) {
			return -isLowerCase;
		}

		return Integer.compare(c1, c2);
	}

	static int compareCompletionProposal(ICompletionProposal o1, ICompletionProposal o2) {
		PrimitiveIterator.OfInt cs1 = o1.getDisplayString().codePoints().filter(c -> !Character.isWhitespace(c))
				.iterator();
		PrimitiveIterator.OfInt cs2 = o2.getDisplayString().codePoints().filter(c -> !Character.isWhitespace(c))
				.iterator();

		while (cs1.hasNext() || cs2.hasNext()) {
			int has = Boolean.compare(cs1.hasNext(), cs2.hasNext());
			if (has != 0) {
				return has;
			}
			int c1 = cs1.nextInt();
			int c2 = cs2.nextInt();

			int comp = compareCharCustom(c1, c2);
			if (comp != 0) {
				return comp;
			}
		}
		return 0;

	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		PluginConfig config = PluginConfig.load();

		List<ICompletionProposal> list = new ArrayList<>(computeUroboroSQLCompletionProposals(viewer, offset, config));
		list.sort(UroboroSQLContentAssistProcessor::compareCompletionProposal);
		if (original != null) {
			Collections.addAll(list, original.computeCompletionProposals(viewer, offset));
		}

		return list.toArray(new ICompletionProposal[list.size()]);
	}

	private List<ICompletionProposal> computeUroboroSQLCompletionProposals(ITextViewer viewer, int offset,
			PluginConfig config) {
		try {
			Document document = new Document(viewer.getDocument(), offset);
			Token userOffsetToken = document.getUserOffsetToken();
			if (userOffsetToken.getType() == TokenType.M_COMMENT) {
				return wrapSignature(
						MCommentTypes.computeCompletionProposals(userOffsetToken.toDocumentPoint(), config));
			}
			int a;
			// TODO
			return Collections.emptyList();

		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private List<ICompletionProposal> wrapSignature(List<ICompletionProposal> computeUroboroSQLCompletionProposals) {
		List<ICompletionProposal> result = new ArrayList<>();
		for (ICompletionProposal completionProposal : computeUroboroSQLCompletionProposals) {
			result.add(wrapSignature(completionProposal));
		}
		return result;
	}

	private static class UsqlCompletionProposal implements ICompletionProposal {
		private final ICompletionProposal completionProposal;

		public UsqlCompletionProposal(ICompletionProposal completionProposal) {
			this.completionProposal = completionProposal;
		}

		@Override
		public Point getSelection(IDocument document) {
			return completionProposal.getSelection(document);
		}

		@Override
		public Image getImage() {
			return completionProposal.getImage();
		}

		@Override
		public String getDisplayString() {
			return completionProposal.getDisplayString();
		}

		@Override
		public IContextInformation getContextInformation() {
			return completionProposal.getContextInformation();
		}

		@Override
		public String getAdditionalProposalInfo() {
			String base = completionProposal.getAdditionalProposalInfo();

			return (base != null ? base : "")
					+ "<br>"
					+ "<br>"
					+ "- uroboroSQL -";
		}

		@Override
		public void apply(IDocument document) {
			completionProposal.apply(document);
		}
	}

	private ICompletionProposal wrapSignature(ICompletionProposal completionProposal) {
		return new UsqlCompletionProposal(completionProposal);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		if (original == null) {
			return null;
		}
		return original.computeContextInformation(viewer, offset);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		if (original == null) {
			return null;
		}
		return original.getCompletionProposalAutoActivationCharacters();
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		if (original == null) {
			return null;
		}
		return original.getContextInformationAutoActivationCharacters();
	}

	@Override
	public String getErrorMessage() {
		if (original == null) {
			return null;
		}
		return original.getErrorMessage();
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (original == null) {
			return null;
		}
		return original.getContextInformationValidator();
	}
}