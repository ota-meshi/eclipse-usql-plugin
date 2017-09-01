package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Column extends AbstractIdentifier<Column> {
	private final Table table;

	public Column(Table table, String name, String comment, String description) {
		super(name, comment, description);
		this.table = table;
	}

	@Override
	protected Collection<String> getLazyTargetTexts() {
		return Arrays.asList(table.getName(), table.getComment(), table.getDescription());
	}

	@Override
	public String getActDescription() {
		String s = Stream.of(
				table.getName() + " " + Objects.toString(table.getComment(), ""),
				getName() + " " + Objects.toString(getComment(), ""), getDescription())
				.filter(Objects::nonNull)
				.map(str -> str.trim())
				.filter(str -> !str.isEmpty())
				.collect(Collectors.joining("<br>"));
		return s.isEmpty() ? "column name." : s;
	}
}
