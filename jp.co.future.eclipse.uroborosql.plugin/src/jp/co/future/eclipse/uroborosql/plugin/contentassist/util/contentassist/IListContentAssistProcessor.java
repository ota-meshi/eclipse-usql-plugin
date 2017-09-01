package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public interface IListContentAssistProcessor {
	List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint point);
}
