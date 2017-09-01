package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;

public interface IType {

	List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint tokenStart, boolean lazy,
			PluginConfig config);
}
