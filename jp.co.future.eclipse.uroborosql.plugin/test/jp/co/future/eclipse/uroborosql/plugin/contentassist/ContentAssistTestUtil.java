package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.StringList;

public class ContentAssistTestUtil {
	public static List<String> computeCompletionResults(String doc) {
		return computeCompletionResults(doc, PluginConfig.load());
	}

	public static List<String> computeCompletionResults(String doc, PluginConfig config) {
		int offset = doc.indexOf("|");
		if (offset >= 0) {
			doc = doc.substring(0, offset) + doc.substring(offset + 1);
		} else {
			offset = doc.length();
		}

		return computeCompletionResults(doc, offset, config);
	}

	public static List<String> computeCompletionResults(String doc, int offset) {
		return computeCompletionResults(doc, offset, PluginConfig.load());
	}

	public static List<String> computeCompletionResults(String doc, int offset, PluginConfig config) {
		if (offset < 0) {
			offset = doc.length() + offset;
		}

		UroboroSQLContentAssistProcessor assistProcessor = new UroboroSQLContentAssistProcessor();
		ITextViewer textViewer = TestUtil.createTextViewer(doc);

		ICompletionProposal[] completionProposals = assistProcessor.computeCompletionProposals(textViewer, offset,
				config);

		return Arrays.stream(completionProposals)
				.map(p -> {
					IDocument document = TestUtil.createDocument(doc);
					p.apply(document);
					return toSelectionDocument(p.getSelection(document), document);
				})
				.collect(Collectors.toCollection(StringList::new));
	}

	private static String toSelectionDocument(Point point, IDocument document) {
		StringBuilder sb = new StringBuilder(document.get());
		sb.insert(point.x, "|");
		if (point.y > 0) {
			sb.insert(point.x + point.y + 1, "|");
		}
		return sb.toString();
	}

}
