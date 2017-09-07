package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;

public interface IType {

	List<IPointCompletionProposal> computeCompletionProposals(Token token, boolean lazy,
			PluginConfig config);
}
