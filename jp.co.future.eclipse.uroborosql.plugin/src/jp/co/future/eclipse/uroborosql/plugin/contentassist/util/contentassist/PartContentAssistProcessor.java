package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Optional;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public interface PartContentAssistProcessor {
	Optional<ICompletionProposal> computeCompletionProposal(DocumentPoint point);
}
