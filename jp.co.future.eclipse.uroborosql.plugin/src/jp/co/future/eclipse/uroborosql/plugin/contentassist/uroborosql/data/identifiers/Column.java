package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;

public class Column extends AbstractIdentifier<Column> {
	final Table table;

	public Column(Table table, String name, String comment, String description, int priority) {
		super(name, comment, description, priority);
		this.table = table;
	}

	Column(Column id1, Column id2) {
		super(id1, id2);
		table = id1.table;
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

	public Replacement buildSelectColumn(int maxWidths, Function<String, String> reservedCaseFormatter) {
		int widths = Strings.widths(getName());
		if (maxWidths < widths) {
			maxWidths = widths;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Strings.rightTabs(getName(), maxWidths)).append(reservedCaseFormatter.apply("\tAs\t"));
		int cursorPosition = sb.length();// ASの次
		if (getComment() != null) {
			sb.append(Strings.rightTabs(getName(), maxWidths)).append("\t-- ").append(getComment());
		} else {
			sb.append(getName());
		}
		return new Replacement(sb.toString(), cursorPosition, getComment() != null);
	}

	public Replacement buildSetColumn(int maxWidths) {
		int widths = Strings.widths(getName());
		if (maxWidths < widths) {
			maxWidths = widths;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Strings.rightTabs(getName(), maxWidths)).append("\t=\t");

		int cursorPosition = sb.length() + 2;// [/*]の次

		String bind = "/*" + Strings.toCamel(getName()) + "*/''";

		if (getComment() != null) {
			sb.append(Strings.rightTabs(bind, maxWidths + 6)).append("\t-- ").append(getComment());
		} else {
			sb.append(bind);
		}
		return new Replacement(sb.toString(), cursorPosition, getComment() != null);
	}

	public Replacement buildInsertColumn(int maxWidths) {
		int widths = Strings.widths(getName());
		if (maxWidths < widths) {
			maxWidths = widths;
		}
		StringBuilder sb = new StringBuilder();
		if (getComment() != null) {
			sb.append(Strings.rightTabs(getName(), maxWidths)).append("\t-- ").append(getComment());
		} else {
			sb.append(getName());
		}
		return new Replacement(sb.toString(), getName().length(), getComment() != null);
	}

	public Replacement buildConditionColumn(int maxWidths) {
		int widths = Strings.widths(getName());
		if (maxWidths < widths) {
			maxWidths = widths;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Strings.rightTabs(getName(), maxWidths)).append("\t=\t");

		int cursorPosition = sb.length() + 2;// [/*]の次

		String bind = "/*" + Strings.toCamel(getName()) + "*/''";

		if (getComment() != null) {
			sb.append(Strings.rightTabs(bind, maxWidths + 6)).append("\t-- ").append(getComment());
		} else {
			sb.append(bind);
		}
		return new Replacement(sb.toString(), cursorPosition, getComment() != null);
	}

}
