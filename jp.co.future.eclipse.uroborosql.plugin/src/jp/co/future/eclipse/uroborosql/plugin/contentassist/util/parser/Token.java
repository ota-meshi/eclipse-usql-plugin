package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.Objects;
import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.SqlConstants;
import jp.co.future.eclipse.uroborosql.plugin.utils.Iterables;
import jp.co.future.eclipse.uroborosql.plugin.utils.Iterators;

public class Token {
	private final TokenType type;
	private final int start;
	private final int end;
	private final Document document;
	private String string;

	public Token(Document document, int start, int end, TokenType type) {
		this.document = document;
		this.type = type;
		this.start = start;
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

	public boolean isIn(int index) {
		return start <= index && index <= end;
	}

	public DocumentPoint toDocumentPoint() {
		return new DocumentPoint(document, getStart());
	}

	public String getString() {
		return string != null ? string : (string = document.substring(getStart(), end + 1));
	}

	public Optional<Token> getNextToken() {
		return document.getTokens().stream()
				.filter(t -> t.isIn(end + 1))
				.findFirst();
	}

	public Optional<Token> getPrevToken() {
		return document.getTokens().stream()
				.filter(t -> t.isIn(start - 1))
				.findFirst();
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStart(), type, getString());
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
		if (getStart() != other.getStart()) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (Objects.equals(getString(), other.getString())) {
			return false;
		}
		return true;
	}

	public static Iterable<Token> getPrevSiblings(Token token) {
		return Iterables.asIterables(() -> Iterators.asIterator(token, Token::getPrevSibling));
	}

	public static Optional<Token> getPrevSibling(Token token) {
		if (token.getType() == TokenType.SYMBOL && token.getString().equals(")")) {
			return getOpenParenthesis(token);
		}
		Optional<Token> prev = token.getPrevToken();
		while (prev.isPresent()) {
			Token prevToken = prev.get();
			if (prevToken.getType().isSqlEnable()) {
				if (prevToken.getType() == TokenType.SYMBOL) {
					String s = prevToken.getString();
					if (s.equals("(")) {
						return Optional.empty();
					}
				}
				return prev;
			}
			prev = prevToken.getPrevToken();
		}
		return Optional.empty();
	}

	private static Optional<Token> getOpenParenthesis(Token token) {
		Optional<Token> prev = token.getPrevToken();
		while (prev.isPresent()) {
			Token prevToken = prev.get();
			if (prevToken.getType().isSqlEnable()) {
				if (prevToken.getType() == TokenType.SYMBOL) {
					String s = prevToken.getString();
					if (s.equals("(")) {
						return prev;
					} else if (s.equals(")")) {
						Optional<Token> open = getOpenParenthesis(prevToken);
						if (!open.isPresent()) {
							return Optional.empty();
						}
						prevToken = open.get();
					}
				}
			}
			prev = prevToken.getPrevToken();
		}
		return Optional.empty();
	}

	public boolean isReservedWord() {
		return getType() == TokenType.SQL_TOKEN && SqlConstants.SQL_RESERVED_WORDS.contains(getString().toUpperCase());
	}
}
