package jp.co.future.eclipse.uroborosql.plugin.sqleditor;

import org.eclipse.datatools.sqltools.sqleditor.SQLEditor;
import org.eclipse.datatools.sqltools.sqleditor.internal.editor.SQLSourceViewerConfiguration;
import org.eclipse.datatools.sqltools.sqleditor.internal.sql.ISQLPartitions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.UroboroSQLContentAssistProcessor;

public class SQLSourceViewerConfigurationExt extends SQLSourceViewerConfiguration {

	public SQLSourceViewerConfigurationExt() {
	}

	public SQLSourceViewerConfigurationExt(SQLEditor editor) {
		super(editor);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = (ContentAssistant) super.getContentAssistant(sourceViewer);

		IContentAssistProcessor original = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new UroboroSQLContentAssistProcessor(original),
				IDocument.DEFAULT_CONTENT_TYPE);

		assistant.setContentAssistProcessor(new UroboroSQLContentAssistProcessor(),
				ISQLPartitions.SQL_MULTILINE_COMMENT);

		return assistant;
	}

}
