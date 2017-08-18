package jp.co.future.eclipse.uroborosql.plugin.contentassist.util;

public class DocumentPoint {
	private final Document document;
	private final int point;
	private String range;

	public DocumentPoint(Document document, int offset) {
		this.document = document;
		point = offset;
	}

	public Document getDocument() {
		return document;
	}

	public int point() {
		return point;
	}

	public String getRangeText() {
		return range != null ? range : (range = document.getRangeText(point));
	}

	@Override
	public String toString() {
		if (point <= document.getUserOffset()) {
			return "left(" + getRangeText() + ")";
		} else {
			return "right(" + getRangeText() + ")";
		}
	}

	public String getIndent() {
		DocumentScanner scanner = new DocumentScanner(document, point);
		while (scanner.hasPrevious()) {
			char c = scanner.previous();
			if (c == '\n' || c == '\r') {
				scanner.next();
				return buildIndent(scanner);
			}
		}
		return buildIndent(scanner);
	}

	private static String buildIndent(DocumentScanner scanner) {
		char c = scanner.current();
		if (!Character.isWhitespace(c)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(c);
		while (scanner.hasNext()) {
			c = scanner.next();
			if (!Character.isWhitespace(c)) {
				return builder.toString();
			}
			builder.append(c);
		}
		return builder.toString();
	}
}