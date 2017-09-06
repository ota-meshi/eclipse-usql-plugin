package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.IPartContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.LazySearchContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.TokenContentAssistProcessor;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Lists;

public abstract class AbstractIdentifier<S extends IIdentifier<S>> implements IIdentifier<S> {

	private final String name;
	private final String comment;
	private final String description;
	private final int commentPriority;
	private final int descriptionPriority;

	public AbstractIdentifier(String name, String comment, String description, int priority) {
		this(name, comment, description, priority, priority);
	}

	private AbstractIdentifier(String name, String comment, String description, int commentPriority,
			int descriptionPriority) {
		if (name == null) {
			throw new NullPointerException("name is null");
		}
		this.name = name;
		this.comment = comment;
		this.description = description;
		this.commentPriority = commentPriority;
		this.descriptionPriority = descriptionPriority;
	}

	AbstractIdentifier(AbstractIdentifier<?> id1, AbstractIdentifier<?> id2) {
		AbstractIdentifier<?> comment = Lists.asList(id1, id2)
				.sorted(Comparator.comparing(id -> id.commentPriority))
				.filter(id -> Strings.isNotEmpty(id.getComment()))
				.findFirst()
				.orElse(id1);
		AbstractIdentifier<?> description = Lists.asList(id1, id2)
				.sorted(Comparator.comparing(id -> id.descriptionPriority))
				.filter(id -> Strings.isNotEmpty(id.getDescription()))
				.findFirst()
				.orElse(id1);
		this.name = id1.name;
		this.comment = comment.getComment();
		this.description = description.getDescription();
		this.commentPriority = comment.commentPriority;
		this.descriptionPriority = description.descriptionPriority;
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
	public Replacement toReplacement() {
		if (getComment() != null) {
			return new Replacement(getName() + "\t-- " + getComment(), getName().length(), true);
		} else {
			return new Replacement(getName(), false);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IPartContentAssistProcessor> createContentAssistProcessor(
			List<IdentifierReplacement<S>> buildReplacements) {
		return buildReplacements.stream()
				.map(e -> {
					return new TokenContentAssistProcessor(getName(),
							() -> e.buildReplacement.apply((S) this),
							e.buildDisplay.apply((S) this),
							() -> getActDescription());
				})
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IPartContentAssistProcessor> createLazyContentAssistProcessor(
			List<IdentifierReplacement<S>> buildReplacements) {
		Set<String> texts = Stream.of(getName(), getComment(), getDescription())
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		getLazyTargetTexts().stream()
				.filter(Objects::nonNull)
				.forEach(texts::add);

		return buildReplacements.stream()
				.map(e -> {
					return new LazySearchContentAssistProcessor(getName(), texts,
							() -> e.buildReplacement.apply((S) this),
							e.buildDisplay.apply((S) this),
							() -> getActDescription());
				})
				.collect(Collectors.toList());
	}

	protected Collection<String> getLazyTargetTexts() {
		return Collections.emptyList();
	}
}
