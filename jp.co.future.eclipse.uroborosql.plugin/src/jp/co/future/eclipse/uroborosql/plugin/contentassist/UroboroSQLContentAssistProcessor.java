package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorPlugin;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorPart;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type.MCommentTypes;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;

public class UroboroSQLContentAssistProcessor implements IContentAssistProcessor {
	private final IContentAssistProcessor original;

	public UroboroSQLContentAssistProcessor() {
		this(null);
	}

	public UroboroSQLContentAssistProcessor(IContentAssistProcessor original) {
		this.original = original;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {


		IEditorPart part = SQLEditorPlugin.getActiveEditor();
		part.


		List<ICompletionProposal> list = new ArrayList<>(computeUroboroSQLCompletionProposals(viewer, offset));
		if (original != null) {
			Collections.addAll(list, original.computeCompletionProposals(viewer, offset));
		}

		return list.toArray(new ICompletionProposal[list.size()]);
	}

	private List<ICompletionProposal> computeUroboroSQLCompletionProposals(ITextViewer viewer, int offset) {
		try {
			Document document = new Document(viewer.getDocument(), offset);
			Token userOffsetToken = document.getUserOffsetToken();
			if (userOffsetToken.getType() == TokenType.M_COMMENT) {
				return MCommentTypes.computeCompletionProposals(userOffsetToken.toDocumentPoint());
			}
			int a;
			// TODO
			return Collections.emptyList();

		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		if (original == null) {
			return null;
		}
		return original.computeContextInformation(viewer, offset);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		if (original == null) {
			return null;
		}
		return original.getCompletionProposalAutoActivationCharacters();
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		if (original == null) {
			return null;
		}
		return original.getContextInformationAutoActivationCharacters();
	}

	@Override
	public String getErrorMessage() {
		if (original == null) {
			return null;
		}
		return original.getErrorMessage();
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (original == null) {
			return null;
		}
		return original.getContextInformationValidator();
	}

	private IProject getProect(ITextEditor editor) {
		IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		IFile file = editorInput.getFile();
		return file.getProject();
	}
}