package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;

public enum MCommentTypes implements IType {
	SYNTAX {
		@Override
		protected List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!lazy && UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}

			List<IPointCompletionProposal> result = new ArrayList<>();
			for (IPartContentAssistProcessor syntaxProcessor : UroboroSQLUtils.SYNTAX_PROCESSORS) {
				syntaxProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
			}
			return result;
		}
	},
	SCRIPT {
		@Override
		protected List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}
			DocumentPoint scriptStart = UroboroSQLUtils.getScriptStartPoint(commentStart.getDocument());
			if (scriptStart == null) {
				return Collections.emptyList();
			}

			return UroboroSQLUtils
					.getScriptAssistProcessors(commentStart.getDocument(), lazy, config)
					.computeCompletionProposals(scriptStart);
		}
	},
	VARIABLE {
		@Override
		protected List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!lazy && UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}

			List<IPointCompletionProposal> result = new ArrayList<>();
			for (IPartContentAssistProcessor varriableProcessor : UroboroSQLUtils
					.getAllVariableAssistProcessors(commentStart.getDocument(), lazy, config)) {
				varriableProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
			}
			return result;

		}
	},
	IDENTIFIER {
		@Override
		protected List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!lazy && UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}

			String text = "/* " + config.getSqlId() + " */";
			TokenContentAssistProcessor assistProcessor = new TokenContentAssistProcessor(text, false,
					() -> "SQL ID token");

			return assistProcessor.computeCompletionProposal(commentStart).map(Arrays::asList)
					.orElse(Collections.emptyList());
		}
	};

	public static final List<IType> TYPES = Arrays.asList(SYNTAX, SCRIPT, VARIABLE, IDENTIFIER);

	@Override
	public List<IPointCompletionProposal> computeCompletionProposals(Token token, boolean lazy, PluginConfig config) {
		return computeCompletionProposals(token.toDocumentPoint(), lazy, config);
	}

	protected abstract List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart,
			boolean lazy,
			PluginConfig config);

}