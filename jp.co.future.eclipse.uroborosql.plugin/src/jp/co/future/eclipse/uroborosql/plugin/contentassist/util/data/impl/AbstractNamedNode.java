package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl;

import java.util.function.Supplier;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.INamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.NodeLevel;

public abstract class AbstractNamedNode<N extends AbstractNamedNode<N>> implements INamedNode {
	private final NodeLevel level;
	private final String name;
	private Supplier<String> getAdditionalProposalInfo;

	public AbstractNamedNode(NodeLevel level, String name) {
		this.name = name;
		this.level = level;
	}

	@Override
	public final String name() {
		return name;
	}

	@Override
	public final String additionalProposalInfo() {
		return getAdditionalProposalInfo != null ? getAdditionalProposalInfo.get() : null;
	}

	@SuppressWarnings("unchecked")
	public final N setGetAdditionalProposalInfo(Supplier<String> getAdditionalProposalInfo) {
		this.getAdditionalProposalInfo = getAdditionalProposalInfo;
		return (N) this;
	}

	public final N setAdditionalProposalInfo(String additionalProposalInfo) {
		return setGetAdditionalProposalInfo(() -> additionalProposalInfo);
	}

	@Override
	public final INamedNode getTokenChild(String token) {
		return children()
				.filter(nn -> nn.isMatchToken(token))
				.findFirst()
				.orElse(null);
	}

	@Override
	public final NodeLevel nodeLevel() {
		return this.level;
	}

	@Override
	public void marge(INamedNode node) {
		String newAdditionalProposalInfo = node.additionalProposalInfo();
		int newlen = newAdditionalProposalInfo != null ? newAdditionalProposalInfo.length() : -1;
		String additionalProposalInfo = node.additionalProposalInfo();
		int len = additionalProposalInfo != null ? additionalProposalInfo.length() : -1;
		if (newlen > len) {
			setAdditionalProposalInfo(newAdditionalProposalInfo);
		}

		marge0(node);

	}

	protected abstract void marge0(INamedNode node);

	@Override
	public String toString() {
		return createAssistText().getReplacementString();
	}

}
