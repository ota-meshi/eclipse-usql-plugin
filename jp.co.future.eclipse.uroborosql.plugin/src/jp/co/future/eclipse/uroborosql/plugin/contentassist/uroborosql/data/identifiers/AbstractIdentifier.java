package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.LazySearchContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;

public abstract class AbstractIdentifier<S extends IIdentifier<S>> implements IIdentifier<S> {
	private final String name;
	private final String comment;
	private final String description;

	public AbstractIdentifier(String name, String comment, String description) {
		this.name = name;
		this.comment = comment;
		this.description = description;
	}

	public AbstractIdentifier(String name) {
		this(name, null);
	}

	public AbstractIdentifier(String name, String comment) {
		this(name, comment, null);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, comment, description);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractIdentifier<?> other = (AbstractIdentifier<?>) obj;
		if (!Objects.equals(name, other.name)) {
			return false;
		} else if (!Objects.equals(comment, other.comment)) {
			return false;
		} else if (!Objects.equals(description, other.description)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (getComment() != null) {
			return getName() + "\t-- " + getComment();
		} else {
			return getName();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IPartContentAssistProcessor> createContentAssistProcessor(
			Map<String, Function<S, Replacement>> buildReplacements) {
		String displayText = toString();

		return buildReplacements.entrySet().stream()
				.map(e -> {
					return new TokenContentAssistProcessor(getName(),
							() -> e.getValue().apply((S) this),
							e.getKey().isEmpty() ? displayText : e.getKey() + " " + displayText,
							() -> getActDescription());
				})
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IPartContentAssistProcessor> createLazyContentAssistProcessor(
			Map<String, Function<S, Replacement>> buildReplacements) {
		Set<String> texts = Stream.of(getName(), getComment(), getDescription())
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		getLazyTargetTexts().stream()
				.filter(Objects::nonNull)
				.forEach(texts::add);

		String displayText = toString();

		return buildReplacements.entrySet().stream()
				.map(e -> {
					return new LazySearchContentAssistProcessor(getName(), texts,
							() -> e.getValue().apply((S) this),
							e.getKey().isEmpty() ? displayText : e.getKey() + " " + displayText,
							() -> getActDescription());
				})
				.collect(Collectors.toList());
	}

	protected Collection<String> getLazyTargetTexts() {
		return Collections.emptyList();
	}
}
