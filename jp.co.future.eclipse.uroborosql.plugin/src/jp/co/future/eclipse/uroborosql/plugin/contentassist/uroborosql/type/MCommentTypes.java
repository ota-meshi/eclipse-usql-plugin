package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public class MCommentTypes {
	private static final IMCommentType SYNTAX = (commentStart, config) -> {
		List<ICompletionProposal> result = new ArrayList<>();
		for (PartContentAssistProcessor syntaxProcessor : UroboroSQLUtils.SYNTAX_PROCESSORS) {
			syntaxProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
		}
		return result;
	};

	private static final IMCommentType SCRIPT = (commentStart, config) -> {
		if (!UroboroSQLUtils.withinScript(commentStart)) {
			return Collections.emptyList();
		}
		DocumentPoint scriptStart = UroboroSQLUtils.getScriptStartPoint(commentStart.getDocument());

		return UroboroSQLUtils
				.getScriptAssistProcessors(commentStart.getDocument(), config).computeCompletionProposals(scriptStart);
	};
	private static final IMCommentType VARIABLE = (commentStart, config) -> {
		List<ICompletionProposal> result = new ArrayList<>();
		for (PartContentAssistProcessor varriableProcessor : UroboroSQLUtils
				.getAllVariableAssistProcessors(commentStart.getDocument(), config)) {
			varriableProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
		}
		return result;

	};
	private static final IMCommentType IDENTIFIER = (commentStart, config) -> {
		String text = "/* " + config.getSqlId() + " */";
		TokenContentAssistProcessor assistProcessor = new TokenContentAssistProcessor(text, "SQL ID token");

		return assistProcessor.computeCompletionProposal(commentStart).map(Arrays::asList)
				.orElse(Collections.emptyList());
	};

	private static final List<IMCommentType> TYPES = Arrays.asList(SYNTAX, SCRIPT, VARIABLE, IDENTIFIER);

	public static List<ICompletionProposal> computeCompletionProposals(DocumentPoint commentStart,
			PluginConfig config) {
		List<ICompletionProposal> result = new ArrayList<>();
		for (IMCommentType assistType : TYPES) {
			result.addAll(assistType.computeCompletionProposals(commentStart, config));
		}
		return result;
	}
}