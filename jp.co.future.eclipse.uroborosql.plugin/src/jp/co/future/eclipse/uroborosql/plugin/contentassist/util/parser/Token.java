package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.SqlConstants;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentIterable;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.FluentList;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterables;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

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

	public boolean isReservedWord() {
		return getType() == TokenType.SQL_TOKEN && SqlConstants.SQL_RESERVED_WORDS.contains(getString().toUpperCase());
	}

	public String getNormalizeString() {
		return getType().getNormalizeString(getString());
	}

	public boolean isBefore(Token token) {
		return start < token.start;
	}

	public boolean isAfter(Token token) {
		return start > token.start;
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

	public static FluentIterable<Token> getNextSiblings(Token token) {
		return Iterables.asIterables(() -> Iterators.asIteratorFromNext(token, Token::getNextSibling));
	}

	public static FluentIterable<Token> getPrevSiblings(Token token) {
		return Iterables.asIterables(() -> Iterators.asIteratorFromNext(token, Token::getPrevSibling));
	}

	public static FluentIterable<Token> getPrevSiblingOrParents(Token token) {
		return Iterables.asIterables(() -> Iterators.asIteratorFromNext(token, Token::getPrevSiblingOrParent));
	}

	public static Optional<Token> getPrevSiblingOrParent(Token token) {
		Optional<Token> prev = Token.getPrevSibling(token);
		if (prev.isPresent()) {
			return prev;
		}
		return token.getPrevToken();

	}

	public static Optional<Token> getNextSibling(Token token) {
		if (isOpenParenthesis(token)) {
			return getCloseParenthesis(token);
		}
		Optional<Token> next = token.getNextToken();
		if (next.isPresent()) {
			if (isCloseParenthesis(next.get())) {
				return Optional.empty();
			}
			return next;
		}
		return Optional.empty();
	}

	public static Optional<Token> getPrevSibling(Token token) {
		if (isCloseParenthesis(token)) {
			return getOpenParenthesis(token);
		}
		Optional<Token> prev = token.getPrevToken();
		if (prev.isPresent()) {
			if (isOpenParenthesis(prev.get())) {
				return Optional.empty();
			}
			return prev;
		}
		return Optional.empty();
	}

	private static Optional<Token> getOpenParenthesis(Token token) {
		Optional<Token> prev = token.getPrevToken();
		while (prev.isPresent()) {
			Token prevToken = prev.get();
			if (prevToken.getType().isSqlEnable()) {
				if (isOpenParenthesis(prevToken)) {
					return prev;
				} else if (isCloseParenthesis(prevToken)) {
					Optional<Token> open = getOpenParenthesis(prevToken);
					if (!open.isPresent()) {
						return Optional.empty();
					}
					prevToken = open.get();
				}
			}
			prev = prevToken.getPrevToken();
		}
		return Optional.empty();
	}

	private static Optional<Token> getCloseParenthesis(Token token) {
		Optional<Token> next = token.getNextToken();
		while (next.isPresent()) {
			Token nextToken = next.get();
			if (nextToken.getType().isSqlEnable()) {
				if (isCloseParenthesis(nextToken)) {
					return next;
				} else if (isOpenParenthesis(nextToken)) {
					Optional<Token> close = getCloseParenthesis(nextToken);
					if (!close.isPresent()) {
						return Optional.empty();
					}
					nextToken = close.get();
				}
			}
			next = nextToken.getNextToken();
		}
		return Optional.empty();
	}

	public static FluentList<Token> getBetweenTokens(Token startToken, Token endToken) {
		List<Token> result = new ArrayList<>();
		Token tgt = startToken;
		while (tgt != null) {
			result.add(tgt);
			if (tgt.equals(endToken)) {
				break;
			}
			tgt = tgt.getNextToken().orElse(null);
		}
		return FluentList.from(result);
	}

	public static class TokenRange {
		private final Token start;
		private final Token end;

		public TokenRange(Token start, Token end) {
			this.start = start;
			this.end = end;
		}

		public Token getStart() {
			return start;
		}

		public Token getEnd() {
			return end;
		}

		@Override
		public String toString() {
			if (end == null) {
				return start.toString();
			}
			return getBetweenTokens()
					.map(t -> t.toString())
					.collect(Collectors.joining());
		}

		public FluentList<Token> getBetweenTokens() {
			return Token.getBetweenTokens(start, end);
		}
	}

	public static FluentIterable<TokenRange> getInParenthesis(Token openParenthesis) {
		Token close = getCloseParenthesis(openParenthesis).orElse(null);
		Token startToken = openParenthesis.getNextToken().orElse(null);
		if (startToken == null) {
			return FluentIterable.empty();
		}
		Token commmaToken = Token.getNextSiblings(startToken)
				.filter(p -> Token.isComma(p))
				.findFirst()
				.orElse(null);
		if (commmaToken == null) {
			return FluentIterable.of(new TokenRange(startToken, null));
		}

		TokenRange tokenRange = new TokenRange(startToken, commmaToken.getPrevToken().orElse(null));

		return Iterables.asIterables(() -> Iterators.asIterator(tokenRange, tr -> {
			if (tr.getEnd() == null) {
				return Optional.empty();
			}

			Token start = tr.getEnd().getNextToken()
					.map(commma -> commma.getNextToken().orElse(null))
					.orElse(null);
			if (start == null) {
				return Optional.empty();
			}
			if (close != null && (close.isBefore(start) || close.equals(start))) {
				return Optional.empty();
			}

			Token end = Token.getNextSiblings(start)
					.filter(p -> Token.isComma(p))
					.findFirst()
					.map(en -> en.getPrevToken().orElse(null))
					.orElse(Token.getNextSiblings(start).findLast().orElse(null));

			return Optional.of(new TokenRange(start, end));
		}));
	}

	public static boolean isInsertWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("INSERT");
	}

	public static boolean isIntoWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("INTO");
	}

	public static boolean isUpdateWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("UPDATE");
	}

	public static boolean isSelectWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("SELECT");
	}

	public static boolean isValuesWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("VALUES");
	}

	public static boolean isSetWord(Token token) {
		return token.getType() == TokenType.SQL_TOKEN && token.getString().equalsIgnoreCase("SET");
	}

	public static boolean isOpenParenthesis(Token token) {
		return token.getType() == TokenType.SYMBOL && token.getString().equals("(");
	}

	public static boolean isCloseParenthesis(Token token) {
		return token.getType() == TokenType.SYMBOL && token.getString().equals(")");
	}

	public static boolean isComma(Token token) {
		return token.getType() == TokenType.SYMBOL && token.getString().equals(",");
	}
}
