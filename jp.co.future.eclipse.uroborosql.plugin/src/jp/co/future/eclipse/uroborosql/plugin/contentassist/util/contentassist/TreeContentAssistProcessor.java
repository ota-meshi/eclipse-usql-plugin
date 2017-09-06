package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TreeContentAssistProcessor implements IListContentAssistProcessor {
	private static class LazyPointProp {
		private final int lazyPoint;
		private final Prop prop;

		LazyPointProp(int lazyPoint, Prop prop) {
			this.lazyPoint = lazyPoint;
			this.prop = prop;
		}
	}

	private interface PropAccessor {
		Prop createOrGet(String name, Supplier<String> additionalProposalInfo);

		Prop get(String name);

		Collection<LazyPointProp> getAssists(String name);
	}

	private static class Prop implements PropAccessor {
		private final String name;
		private final Supplier<String> additionalProposalInfo;

		private final Map<String, Prop> children = new HashMap<>();

		private Prop(String name, Supplier<String> additionalProposalInfo) {
			this.name = name;
			this.additionalProposalInfo = additionalProposalInfo;
		}

		@Override
		public Prop createOrGet(String name, Supplier<String> additionalProposalInfo) {
			return children
					.computeIfAbsent(name, n -> new Prop(name, additionalProposalInfo));
		}

		@Override
		public Prop get(String name) {
			return children.get(name);
		}

		@Override
		public Collection<LazyPointProp> getAssists(String name) {
			List<LazyPointProp> result = new ArrayList<>();
			children.forEach((k, v) -> {
				OptionalInt point = HitTester.hit(name, k);
				if (!point.isPresent()) {
					return;
				}
				result.add(new LazyPointProp(point.getAsInt(), v));
			});

			return result;
		}

		//		@Override
		//		public String toString() {
		//			return "Prop [name=" + name + "]";
		//		}
	}

	private final Map<String, Prop> props = new HashMap<>();
	private final PropAccessor root = new PropAccessor() {

		@Override
		public Prop createOrGet(String name, Supplier<String> additionalProposalInfo) {
			return props
					.computeIfAbsent(name, n -> new Prop(name, additionalProposalInfo));
		}

		@Override
		public Prop get(String name) {
			return props.get(name);
		}

		@Override
		public Collection<LazyPointProp> getAssists(String name) {
			List<LazyPointProp> result = new ArrayList<>();
			props.forEach((k, v) -> {
				OptionalInt point = HitTester.hit(name, k);
				if (!point.isPresent()) {
					return;
				}
				result.add(new LazyPointProp(point.getAsInt(), v));
			});

			return result;
		}
	};

	public void addAll(Collection<String> props, Supplier<String> additionalProposalInfo) {
		props.forEach(p -> add(p, additionalProposalInfo));
	}

	public void add(String props, Supplier<String> additionalProposalInfo) {
		add(props.split("\\.", -1), additionalProposalInfo);
	}

	public void add(String[] props, Supplier<String> additionalProposalInfo) {
		PropAccessor prop = root;

		for (String propName : props) {
			prop = prop.createOrGet(propName, additionalProposalInfo);
		}
	}

	@Override
	public List<IPointCompletionProposal> computeCompletionProposals(DocumentPoint point) {
		PropAccessor propAccess = root;
		String[] userProps = point.getRangeText().split("\\.", -1);
		List<String> replacementProps = new ArrayList<>();

		for (int i = 0; i < userProps.length - 1; i++) {
			Prop prop = propAccess.get(userProps[i]);
			if (prop == null) {
				return Collections.emptyList();
			}
			replacementProps.add(prop.name);
			propAccess = prop;
		}
		String endPropName = userProps[userProps.length - 1];
		Collection<LazyPointProp> hits = propAccess.getAssists(endPropName);
		int replacementLength = point.getDocument().getUserOffset() - point.point();

		return hits.stream()
				.filter(p -> !endPropName.equals(p.prop.name))
				.map(p -> {
					List<String> replacementProps2 = new ArrayList<>(replacementProps);
					replacementProps2.add(p.prop.name);
					String replacementString = String.join(".", replacementProps2);
					int cursorPosition = replacementString.lastIndexOf(")");
					if (cursorPosition < 0) {
						cursorPosition = replacementString.length();
					}
					return new CompletionProposal(p.lazyPoint, new String[] { replacementString }, point.point(),
							replacementLength,
							cursorPosition, false, p.prop.name, p.prop.additionalProposalInfo.get());
				})
				.collect(Collectors.toList());

	}

}
