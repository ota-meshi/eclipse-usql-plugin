package jp.co.future.eclipse.uroborosql.plugin.contentassist.util;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;

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

	public Token getToken() {
		return getDocument().getTokens().stream()
				.filter(t -> t.isIn(point))
				.findFirst().orElse(null);
	}

}