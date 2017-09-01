package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public interface IPartContentAssistProcessor {
	Optional<IPointCompletionProposal> computeCompletionProposal(DocumentPoint point);
}
