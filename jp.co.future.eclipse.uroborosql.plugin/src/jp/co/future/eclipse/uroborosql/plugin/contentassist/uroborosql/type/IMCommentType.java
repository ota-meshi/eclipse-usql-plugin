package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public interface IMCommentType {

	List<ICompletionProposal> computeCompletionProposals(DocumentPoint commentStart, PluginConfig config);
}
