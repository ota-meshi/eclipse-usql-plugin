package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;
import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;

public class Table extends AbstractIdentifier<Table> {
	final PluginConfig config;

	public Table(PluginConfig config, String name, String comment, String description) {
		super(name, comment, description);
		this.config = config;
	}

	@Override
	public String getActDescription() {
		String s = Stream.of(
				getName() + " " + Objects.toString(getComment(), ""), getDescription())
				.filter(Objects::nonNull)
				.map(str -> str.trim())
				.filter(str -> !str.isEmpty())
				.collect(Collectors.joining("<br>"));
		return s.isEmpty() ? "table name." : s;
	}

	public Columns getColumns() {
		return config.getColumn(this);
	}

	public Replacement buildSelectSql(Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> Strings.widths(s))
				.max().orElse(0);
		Replacement tableReplacement = toReplacement();

		result.add("");
		result.addAll(selectCols(columns, maxWidths, reservedCaseFormatter));
		result.add(reservedCaseFormatter.apply("From"));
		result.addAll(tableReplacement.getReplacementStrings("", "\t"));

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1, true);
	}

	public Replacement buildUpdateSql(Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> Strings.widths(s))
				.max().orElse(0);
		Replacement tableReplacement = toReplacement();

		result.add("");
		result.addAll(tableReplacement.getReplacementStrings("", "\t"));
		result.add(reservedCaseFormatter.apply("Set"));
		result.addAll(updateCols(columns, maxWidths));

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1, true);
	}

	public Replacement buildInsertSql() {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> Strings.widths(s))
				.max().orElse(0);
		Replacement tableReplacement = toReplacement();

		result.add("");
		result.addAll(tableReplacement.getReplacementStrings("", "\t"));
		result.add("(");
		result.addAll(insertCols(columns, maxWidths));
		result.add(")");

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1, false);
	}

	private List<String> selectCols(List<Column> columns, int maxWidths,
			Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		for (int i = 0; i < columns.size(); i++) {
			Column c = columns.get(i);
			if (i == 0) {
				result.addAll(selectCol("", c, maxWidths, reservedCaseFormatter));
			} else {
				result.addAll(selectCol(",", c, maxWidths, reservedCaseFormatter));
			}
		}
		return result;
	}

	private List<String> selectCol(String prefix, Column column, int maxWidths,
			Function<String, String> reservedCaseFormatter) {
		return column.buildSelectColumn(maxWidths, reservedCaseFormatter).getReplacementStrings(prefix, "\t");
	}

	private List<String> updateCols(List<Column> columns, int maxWidths) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < columns.size(); i++) {
			Column c = columns.get(i);
			if (i == 0) {
				result.addAll(updateCol("", c, maxWidths));
			} else {
				result.addAll(updateCol(",", c, maxWidths));
			}
		}
		return result;
	}

	private List<String> updateCol(String prefix, Column column, int maxWidths) {
		return column.buildSetColumn(maxWidths).getReplacementStrings(prefix, "\t");
	}

	private List<String> insertCols(List<Column> columns, int maxWidths) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < columns.size(); i++) {
			Column c = columns.get(i);
			if (i == 0) {
				result.addAll(insertCol("", c, maxWidths));
			} else {
				result.addAll(insertCol(",", c, maxWidths));
			}
		}
		return result;
	}

	private List<String> insertCol(String prefix, Column column, int maxWidths) {
		return column.buildInsertColumn(maxWidths).getReplacementStrings(prefix, "\t");
	}

	public Map<String, ?> getNameParamMap() {
		return getNameParamMap(getName());
	}

	public static Map<String, ?> getNameParamMap(String name) {
		Map<String, Object> map = new HashMap<>();
		map.put("tableName", name);
		map.put("table_name", name.toLowerCase());
		map.put("TABLE_NAME", name.toUpperCase());

		map.put("tableNm", name);
		map.put("table_nm", name.toLowerCase());
		map.put("TABLE_NM", name.toUpperCase());
		return map;
	}
}
