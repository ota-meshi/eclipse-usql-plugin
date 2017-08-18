package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.FmtContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.PartContentAssistProcessor;

public class MCommentTypes {
	private static final IMCommentType SYNTAX = commentStart -> {
		List<ICompletionProposal> result = new ArrayList<>();
		for (PartContentAssistProcessor syntaxProcessor : UroboroSQLUtils.SYNTAX_PROCESSORS) {
			syntaxProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
		}
		return result;
	};

	private static final IMCommentType SCRIPT = commentStart -> {
		if (!UroboroSQLUtils.withinScript(commentStart)) {
			return Collections.emptyList();
		}
		DocumentPoint scriptStart = UroboroSQLUtils.getScriptStartPoint(commentStart.getDocument());

		return UroboroSQLUtils
				.getScriptAssistProcessors(commentStart.getDocument()).computeCompletionProposals(scriptStart);
	};
	private static final IMCommentType VARIABLE = commentStart -> {
		List<ICompletionProposal> result = new ArrayList<>();
		for (PartContentAssistProcessor varriableProcessor : UroboroSQLUtils
				.getAllVariableAssistProcessors(commentStart.getDocument())) {
			varriableProcessor.computeCompletionProposal(commentStart).ifPresent(result::add);
		}
		return result;

	};
	private static final IMCommentType IDENTIFIER = commentStart -> {
		String text = "/* " + UroboroSQLUtils.getSqlId() + " */";
		FmtContentAssistProcessor assistProcessor = new FmtContentAssistProcessor(text, "uroboroSQL SQL ID");

		return assistProcessor.computeCompletionProposal(commentStart).map(Arrays::asList)
				.orElse(Collections.emptyList());
	};

	private static final List<IMCommentType> TYPES = Arrays.asList(SYNTAX, SCRIPT, VARIABLE, IDENTIFIER);

	public static List<ICompletionProposal> computeCompletionProposals(DocumentPoint commentStart) {
		List<ICompletionProposal> result = new ArrayList<>();
		for (IMCommentType assistType : TYPES) {
			result.addAll(assistType.computeCompletionProposals(commentStart));
		}
		return result;
	}
}