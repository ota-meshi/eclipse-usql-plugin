package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.List;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;

public interface IIdentifier<S extends IIdentifier<S>> {
	public static class IdentifierReplacement<S extends IIdentifier<S>> {
		final Function<S, String> buildDisplay;
		final Function<S, Replacement> buildReplacement;

		public IdentifierReplacement(Function<S, Replacement> buildReplacement) {
			this(i -> i.toString(), buildReplacement);
		}

		public IdentifierReplacement(Function<S, String> buildDisplay, Function<S, Replacement> buildReplacement) {
			this.buildDisplay = buildDisplay;
			this.buildReplacement = buildReplacement;
		}

	}

	String getName();

	String getComment();

	String getDescription();

	String getActDescription();

	List<IPartContentAssistProcessor> createContentAssistProcessor(
			List<IdentifierReplacement<S>> buildReplacements);

	List<IPartContentAssistProcessor> createLazyContentAssistProcessor(
			List<IdentifierReplacement<S>> buildReplacements);

	Replacement toReplacement();
}
