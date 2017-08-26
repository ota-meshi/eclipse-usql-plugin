package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

class TokenImpl extends Token {
	private final Document document;
	private final int start;
	private int end;
	private String string;

	TokenImpl(Document document, int start, TokenType type) {
		super(type);
		this.start = start;
		this.document = document;

	}

	@Override
	public DocumentPoint toDocumentPoint() {
		return new DocumentPoint(document, getStart());
	}

	@Override
	public int getStart() {
		return start;
	}

	void setEnd(int end) {
		this.end = end;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public String getString() {
		return string != null ? string : (string = document.substring(getStart(), end + 1));
	}

	@Override
	public boolean isIn(int index) {
		return getStart() <= index && index <= end;
	}

	@Override
	public Optional<Token> getNextToken() {
		return document.getTokens().stream()
				.filter(t -> t.isIn(end + 1))
				.findFirst();
	}

}
