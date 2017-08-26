package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import java.util.Objects;
import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentPoint;

public abstract class Token {
	private final TokenType type;

	public Token(TokenType type) {
		this.type = type;
	}

	public abstract DocumentPoint toDocumentPoint();

	public abstract int getStart();

	public abstract int getEnd();

	public TokenType getType() {
		return type;
	}

	public abstract String getString();

	public abstract boolean isIn(int index);

	public abstract Optional<Token> getNextToken();

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

}
