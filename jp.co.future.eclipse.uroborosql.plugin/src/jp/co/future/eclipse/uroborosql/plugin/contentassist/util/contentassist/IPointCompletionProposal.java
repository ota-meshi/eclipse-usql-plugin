package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public interface IPointCompletionProposal extends ICompletionProposal {
	int getLazyPoint();
}
