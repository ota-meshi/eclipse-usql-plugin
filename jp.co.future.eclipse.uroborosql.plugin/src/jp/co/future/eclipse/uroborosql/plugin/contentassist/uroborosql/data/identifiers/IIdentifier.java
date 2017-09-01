package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;

public interface IIdentifier<S extends IIdentifier<S>> {

	String getName();

	String getComment();

	String getDescription();

	String getActDescription();

	List<IPartContentAssistProcessor> createContentAssistProcessor(
			Map<String, Function<S, Replacement>> buildReplacements);

	List<IPartContentAssistProcessor> createLazyContentAssistProcessor(
			Map<String, Function<S, Replacement>> buildReplacements);
}
