package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public interface ListContentAssistProcessor {
	List<ICompletionProposal> computeCompletionProposals(DocumentPoint point);
}
