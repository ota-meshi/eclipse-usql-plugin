package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.Objects;
import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public class Token {
	private final Document document;
	private final int start;
	private final TokenType type;
	private int end;

	Token(Document document, int start, TokenType type) {
		this.document = document;
		this.start = start;
		this.type = type;
	}

	public DocumentPoint toDocumentPoint() {
		return new DocumentPoint(document, start);
	}

	void setEnd(int end) {
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public TokenType getType() {
		return type;
	}

	public String getString() {
		return document.substring(start, end + 1);
	}

	public boolean isIn(int index) {
		return start <= index && index <= end;
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end, type);
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
		Token other = (Token) obj;
		if (end != other.end) {
			return false;
		}
		if (start != other.start) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public Optional<Token> getNextToken() {
		return document.getTokens().stream()
				.filter(t -> t.isIn(end + 1))
				.findFirst();
	}

}
