package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.UroboroSQLUtils;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class TreeContentAssistProcessor implements ListContentAssistProcessor {
	private interface PropAccessor {
		Prop createOrGet(String name, String additionalProposalInfo);

		Prop get(String name);

		Collection<Prop> getAssists(String name);
	}

	private static class Prop implements PropAccessor {
		private final String name;
		private final String additionalProposalInfo;

		private final Map<String, Prop> children = new HashMap<>();

		private Prop(String name, String additionalProposalInfo) {
			this.name = name;
			this.additionalProposalInfo = additionalProposalInfo;
		}

		private String getName() {
			return name;
		}

		@Override
		public Prop createOrGet(String name, String additionalProposalInfo) {
			return children
					.computeIfAbsent(name, n -> new Prop(name, additionalProposalInfo));
		}

		@Override
		public Prop get(String name) {
			return children.get(name);
		}

		@Override
		public Collection<Prop> getAssists(String name) {
			return children.entrySet().stream()
					.filter(e -> e.getKey().startsWith(name))
					.map(e -> e.getValue())
					.collect(Collectors.toList());
		}

		//		@Override
		//		public String toString() {
		//			return "Prop [name=" + name + "]";
		//		}
	}

	private final Map<String, Prop> props = new HashMap<>();
	private final PropAccessor root = new PropAccessor() {

		@Override
		public Prop createOrGet(String name, String additionalProposalInfo) {
			return props
					.computeIfAbsent(name, n -> new Prop(name, additionalProposalInfo));
		}

		@Override
		public Prop get(String name) {
			return props.get(name);
		}

		@Override
		public Collection<Prop> getAssists(String name) {
			return props.entrySet().stream()
					.filter(e -> e.getKey().startsWith(name))
					.map(e -> e.getValue())
					.collect(Collectors.toList());
		}
	};

	public void addAll(Collection<String> props, String additionalProposalInfo) {
		props.forEach(p -> add(p, additionalProposalInfo));
	}

	public void add(String props, String additionalProposalInfo) {
		add(props.split("\\.", -1), additionalProposalInfo);
	}

	public void add(String[] props, String additionalProposalInfo) {
		PropAccessor prop = root;

		for (String propName : props) {
			prop = prop.createOrGet(propName, additionalProposalInfo);
		}
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(DocumentPoint point) {
		PropAccessor propAccess = root;
		String[] userProps = point.getRangeText().split("\\.", -1);
		List<String> replacementProps = new ArrayList<>();

		for (int i = 0; i < userProps.length - 1; i++) {
			Prop prop = propAccess.get(userProps[i]);
			if (prop == null) {
				return Collections.emptyList();
			}
			replacementProps.add(prop.getName());
			propAccess = prop;
		}
		String endPropName = userProps[userProps.length - 1];
		Collection<Prop> hits = propAccess.getAssists(endPropName);
		int replacementLength = point.getDocument().getUserOffset() - point.point();

		return hits.stream()
				.filter(p -> !endPropName.equals(p.getName()))
				.sorted(Comparator.comparing(Prop::getName))
				.map(p -> {
					List<String> replacementProps2 = new ArrayList<>(replacementProps);
					replacementProps2.add(p.getName());
					String replacementString = String.join(".", replacementProps2);
					int cursorPosition = replacementString.lastIndexOf(")");
					if (cursorPosition < 0) {
						cursorPosition = replacementString.length();
					}
					return new CompletionProposal(replacementString, point.point(), replacementLength, cursorPosition,
							UroboroSQLUtils.getImage(),
							p.getName(),
							/* contextInformation */null,
							p.additionalProposalInfo);
				})
				.collect(Collectors.toList());

	}

}
