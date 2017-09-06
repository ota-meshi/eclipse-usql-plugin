package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type.IType;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type.MCommentTypes;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type.StatementTypes;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPointCompletionProposal;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.TokenType;

public enum ContentAssistProcessors {
	MULTILINE_COMMENT {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(Token userOffsetToken, boolean lazy,
				PluginConfig config) {
			DocumentPoint commentStart = userOffsetToken.toDocumentPoint();
			List<IPointCompletionProposal> result = new ArrayList<>();
			for (IType assistType : MCommentTypes.TYPES) {
				result.addAll(assistType.computeCompletionProposals(commentStart, lazy, config));
			}
			return result;
		}

		@Override
		public boolean possibilityLazy(Token userOffsetToken) {
			return userOffsetToken.getString().replaceAll("\\s", "").startsWith("/*#");
		}
	},
	TOKEN {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(Token userOffsetToken,
				boolean lazy, PluginConfig config) {
			Optional<StatementTypes> tokenTypes = StatementTypes.within(userOffsetToken, this);
			if (tokenTypes.isPresent()) {
				return tokenTypes.get().computeCompletionProposals(userOffsetToken.toDocumentPoint(), lazy, config);
			}

			return Collections.emptyList();
		}

		@Override
		public boolean possibilityLazy(Token userOffsetToken) {
			return true;
		}
	},
	WHITESPACE {
		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(Token userOffsetToken,
				boolean lazy, PluginConfig config) {
			Optional<StatementTypes> tokenTypes = StatementTypes.within(userOffsetToken, this);
			if (tokenTypes.isPresent()) {
				return tokenTypes.get().computeCompletionProposals(userOffsetToken.toDocumentPoint(), lazy, config);
			}

			return Collections.emptyList();
		}

		@Override
		public boolean possibilityLazy(Token userOffsetToken) {
			return false;
		}
	},
	NONE {

		@Override
		public List<IPointCompletionProposal> computeCompletionProposals(Token userOffsetToken, boolean lazy,
				PluginConfig config) {
			return Collections.emptyList();
		}

		@Override
		public boolean possibilityLazy(Token userOffsetToken) {
			return false;
		}

	};
	public abstract List<IPointCompletionProposal> computeCompletionProposals(Token userOffsetToken,
			boolean lazy, PluginConfig config);

	public abstract boolean possibilityLazy(Token userOffsetToken);

	public static ContentAssistProcessors of(Token token) {
		if (token.getType() == TokenType.M_COMMENT) {
			return ContentAssistProcessors.MULTILINE_COMMENT;
		} else if (token.getType() == TokenType.SQL_TOKEN) {
			return ContentAssistProcessors.TOKEN;
		} else if (token.getType() == TokenType.WHITESPACE) {
			return ContentAssistProcessors.WHITESPACE;
		}
		return ContentAssistProcessors.NONE;
	}

}
