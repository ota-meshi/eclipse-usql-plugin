package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.stream.Collectors;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import jp.co.future.eclipse.uroborosql.plugin.UroboroSQLPlugin;
import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.ContentAssistProcessors;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;

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

	static int compareCompletionProposal(IPointCompletionProposal o1, IPointCompletionProposal o2) {
		int pcomp = Integer.compare(o1.getLazyPoint(), o2.getLazyPoint());
		if (pcomp != 0) {
			return pcomp;
		}

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
		return computeCompletionProposals(viewer, offset, config);
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, PluginConfig config) {

		List<ICompletionProposal> list = computeUroboroSQLCompletionProposals(viewer, offset, config).stream()
				.sorted(UroboroSQLContentAssistProcessor::compareCompletionProposal)
				.collect(Collectors.toCollection(ArrayList::new));
		if (original != null) {
			Collections.addAll(list, original.computeCompletionProposals(viewer, offset));
		}

		return list.toArray(new ICompletionProposal[list.size()]);
	}

	private List<IPointCompletionProposal> computeUroboroSQLCompletionProposals(ITextViewer viewer, int offset,
			PluginConfig config) {
		try {
			Document document = new Document(viewer.getDocument(), offset);
			Token userOffsetToken = document.getUserOffsetToken();
			ContentAssistProcessors contentAssistProcessors = ContentAssistProcessors.of(userOffsetToken);
			List<IPointCompletionProposal> completionProposals = contentAssistProcessors.computeCompletionProposals(
					userOffsetToken, false,
					config);
			if (completionProposals.size() <= 3 && contentAssistProcessors.possibilityLazy(userOffsetToken)) {
				completionProposals = contentAssistProcessors.computeCompletionProposals(userOffsetToken, true, config);
			}
			return completionProposals;

		} catch (Exception e) {
			UroboroSQLPlugin.printConsole(e);
			return Collections.emptyList();
		}
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