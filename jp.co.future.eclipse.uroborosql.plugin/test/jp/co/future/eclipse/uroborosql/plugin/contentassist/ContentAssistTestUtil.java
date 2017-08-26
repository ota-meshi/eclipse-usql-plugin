package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.StringList;

public class ContentAssistTestUtil {
	public static List<String> computeCompletionResults(String doc) {
		UroboroSQLContentAssistProcessor assistProcessor = new UroboroSQLContentAssistProcessor();
		ITextViewer textViewer = TestUtil.createTextViewer(doc);
		int offset = doc.length();

		ICompletionProposal[] completionProposals = assistProcessor.computeCompletionProposals(textViewer, offset);

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
		return sb.toString();
	}

}
