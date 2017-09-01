package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.co.future.eclipse.uroborosql.plugin.config.PluginConfig;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist.Replacement;

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

	public List<Column> getColumns() {
		return config.getColumn(this);
	}

	public Replacement buildSelectSql(Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> widths(s))
				.max().orElse(0);

		result.add("");
		result.addAll(selectCols(columns, maxWidths, reservedCaseFormatter));
		result.add(reservedCaseFormatter.apply("From"));
		result.add("\t" + toString());

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1);
	}

	public Replacement buildUpdateSql(Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> widths(s))
				.max().orElse(0);

		result.add("");
		result.add("\t" + toString());
		result.add(reservedCaseFormatter.apply("Set"));
		result.addAll(updateCols(columns, maxWidths));

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1);
	}

	public Replacement buildInsertSql(Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();

		List<Column> columns = getColumns();
		int maxWidths = columns.stream()
				.map(c -> c.getName())
				.mapToInt(s -> widths(s))
				.max().orElse(0);

		result.add("");
		result.add("\t" + toString());
		result.add("(");
		result.addAll(insertCols(columns, maxWidths));
		result.add(")");

		return new Replacement(result, result.stream().mapToInt(s -> s.length()).sum() + result.size() - 1);
	}

	private List<String> selectCols(List<Column> columns, int maxWidths,
			Function<String, String> reservedCaseFormatter) {
		List<String> result = new ArrayList<>();
		columns.stream()
				.map(c -> selectCol(c, maxWidths, reservedCaseFormatter))
				.findFirst().ifPresent(result::add);
		columns.stream()
				.skip(1)
				.map(c -> "," + selectCol(c, maxWidths, reservedCaseFormatter))
				.forEach(result::add);
		return result;
	}

	private String selectCol(Column column, int maxWidths, Function<String, String> reservedCaseFormatter) {
		StringBuilder sb = new StringBuilder("\t");
		sb.append(rightTabs(column.getName(), maxWidths)).append(reservedCaseFormatter.apply("\tAs\t"));
		if (column.getComment() != null) {
			sb.append(rightTabs(column.getName(), maxWidths)).append("\t-- ").append(column.getComment());
		} else {
			sb.append(column.getName());
		}
		return sb.toString();
	}

	private List<String> updateCols(List<Column> columns, int maxWidths) {
		List<String> result = new ArrayList<>();
		columns.stream()
				.map(c -> updateCol(c, maxWidths))
				.findFirst().ifPresent(result::add);
		columns.stream()
				.skip(1)
				.map(c -> "," + updateCol(c, maxWidths))
				.forEach(result::add);
		return result;
	}

	private String updateCol(Column column, int maxWidths) {
		StringBuilder sb = new StringBuilder("\t");
		sb.append(rightTabs(column.getName(), maxWidths)).append("\t=\t");

		String bind = "/*" + column.getName() + "*/''";
		if (column.getComment() != null) {
			sb.append(rightTabs(bind, maxWidths + 6)).append("\t-- ").append(column.getComment());
		} else {
			sb.append(bind);
		}
		return sb.toString();
	}

	private List<String> insertCols(List<Column> columns, int maxWidths) {
		List<String> result = new ArrayList<>();
		columns.stream()
				.map(c -> insertCol(c, maxWidths))
				.findFirst().ifPresent(result::add);
		columns.stream()
				.skip(1)
				.map(c -> "," + insertCol(c, maxWidths))
				.forEach(result::add);
		return result;
	}

	private String insertCol(Column column, int maxWidths) {
		StringBuilder sb = new StringBuilder("\t");
		if (column.getComment() != null) {
			sb.append(rightTabs(column.getName(), maxWidths)).append("\t-- ").append(column.getComment());
		} else {
			sb.append(column.getName());
		}
		return sb.toString();
	}

	private String rightTabs(String name, int maxWidths) {
		int maxTabWidths = (maxWidths & 3) == 0 ? maxWidths : maxWidths - maxWidths % 4 + 4;

		int widths = name.codePoints().map(p -> widths(p)).sum();
		int diff = maxTabWidths - widths;
		int tabCount = diff / 4 + ((diff & 3) == 0 ? 0 : 1);
		StringBuilder sb = new StringBuilder(name);
		for (int i = 0; i < tabCount; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	private int widths(String s) {
		return s.codePoints().map(p -> widths(p)).sum();
	}

	private int widths(int point) {
		if (point == '\t') {
			return 4;
		}

		CharBuffer cb = CharBuffer.wrap(Character.toChars(point));
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.reset();
		ByteBuffer bb = ByteBuffer.allocate((int) (encoder.maxBytesPerChar() * 2));
		encoder.encode(cb, bb, true);
		return bb.position() > 1 ? 2 : 1;
	}

}
