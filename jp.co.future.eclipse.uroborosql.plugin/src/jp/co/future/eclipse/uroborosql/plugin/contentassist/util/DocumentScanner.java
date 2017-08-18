package jp.co.future.eclipse.uroborosql.plugin.contentassist.util;

public class DocumentScanner {
	private final Document document;
	private int index;

	public DocumentScanner(Document document) {
		this.document = document;
		index = -1;
	}

	DocumentScanner(Document document, int offset) {
		this.document = document;
		index = offset;
	}

	public boolean hasPrevious() {
		return index > 0;
	}

	public char previous() {
		index--;
		return current();
	}

	public boolean hasNext() {
		return index + 1 < document.length();
	}

	public char next() {
		index++;
		return current();
	}

	public char current() {
		return document.charAt(index);
	}

	public char offset(int offset) {
		int target = index + offset;
		if (0 <= target && target < document.length()) {
			return document.charAt(target);
		}
		return 0;
	}

	public int index() {
		return index;
	}

	@Override
	public String toString() {
		if (index <= document.getUserOffset()) {
			return "left(" + document.getRangeText(index) + ")";
		} else {
			return "right(" + document.getRangeText(index) + ")";
		}
	}

	public DocumentPoint toDocumentPoint() {
		return new DocumentPoint(document, index);
	}
}