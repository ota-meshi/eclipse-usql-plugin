package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.ListContentAssistProcessor;

public interface IMCommentType extends ListContentAssistProcessor {

	@Override
	List<ICompletionProposal> computeCompletionProposals(DocumentPoint commentStart);
}
