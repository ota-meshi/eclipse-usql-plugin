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

public enum MCommentTypes implements IType {
	SYNTAX {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
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
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}
			DocumentPoint scriptStart = UroboroSQLUtils.getScriptStartPoint(commentStart.getDocument());

			return UroboroSQLUtils
					.getScriptAssistProcessors(commentStart.getDocument(), lazy, config)
					.computeCompletionProposals(scriptStart);
		}
	},
	VARIABLE {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
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
		public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
				PluginConfig config) {
			if (!lazy && UroboroSQLUtils.withinScript(commentStart)) {
				return Collections.emptyList();
			}

			String text = "/* " + config.getSqlId() + " */";
			TokenContentAssistProcessor assistProcessor = new TokenContentAssistProcessor(text, () -> "SQL ID token");

			return assistProcessor.computeCompletionProposal(commentStart).map(Arrays::asList)
					.orElse(Collections.emptyList());
		}
	};

	public static final List<IType> TYPES = Arrays.asList(SYNTAX, SCRIPT, VARIABLE, IDENTIFIER);

	@Override
	public abstract List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint commentStart, boolean lazy,
			PluginConfig config);

}