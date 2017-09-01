package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.DocumentScanner;

public enum TokenType {
	SQL_TOKEN {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (Character.isWhitespace(c) || isSymbol(c)) {
					scanner.previous();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return true;
		}

		@Override
		public boolean isSqlEnable() {
			return true;
		}
	},
	WHITESPACE {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (!Character.isWhitespace(c)) {
					scanner.previous();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return false;
		}

		@Override
		public boolean isSqlEnable() {
			return false;
		}
	},
	NUM_LATERAL {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (!Character.isDigit(c)) {
					scanner.previous();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return true;
		}

		@Override
		public boolean isSqlEnable() {
			return true;
		}
	},
	STR_LATERAL {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (c == '\'' && scanner.offset(1) != '\'') {
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return true;
		}

		@Override
		public boolean isSqlEnable() {
			return true;
		}
	},
	NAME {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (c == '"' && scanner.offset(1) != '"') {
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return true;
		}

		@Override
		public boolean isSqlEnable() {
			return true;
		}
	},
	SYMBOL {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (!isSymbol(c) || c == '\'' || c == '"' || c == '/' && scanner.offset(1) == '*'
						|| c == '-' && scanner.offset(1) == '-') {
					scanner.previous();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return true;
		}

		@Override
		public boolean isSqlEnable() {
			return true;
		}
	},
	L_COMMENT {
		@Override
		public void scanEnd(DocumentScanner scanner) {
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (isLf(c)) {
					break;
				}
			}

			while (scanner.hasNext()) {
				char c = scanner.next();
				if (!isLf(c)) {
					scanner.previous();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return false;
		}

		@Override
		public boolean isSqlEnable() {
			return false;
		}

	},
	M_COMMENT {

		@Override
		public void scanEnd(DocumentScanner scanner) {
			scanner.next();
			while (scanner.hasNext()) {
				char c = scanner.next();
				if (c == '*' && scanner.offset(1) == '/') {
					scanner.next();
					return;
				}
			}
		}

		@Override
		public boolean isVariableNext() {
			return false;
		}

		@Override
		public boolean isSqlEnable() {
			return false;
		}

	},;

	public static TokenType startOf(char c, DocumentScanner scanner) {
		if (Character.isWhitespace(c)) {
			return WHITESPACE;
		}
		if (Character.isDigit(c)) {
			return NUM_LATERAL;
		}
		if (c == '\'') {
			return STR_LATERAL;
		}
		if (c == '"') {
			return NAME;
		}
		if (c == '-' && scanner.offset(1) == '-') {
			return L_COMMENT;
		}
		if (c == '/' && scanner.offset(1) == '*') {
			return M_COMMENT;
		}
		if (isSymbol(c)) {
			return SYMBOL;
		}
		return SQL_TOKEN;
	}

	private static boolean isSymbol(final char c) {
		switch (c) {
		case '"': // double quote
		case '?': // question mark
		case '%': // percent
		case '&': // ampersand
		case '\'': // quote
		case '(': // left paren
		case ')': // right paren
		case '|': // vertical bar
		case '*': // asterisk
		case '+': // plus sign
		case ',': // comma
		case '-': // minus sign
		case '.': // period
		case '/': // solidus
		case ':': // colon
		case ';': // semicolon
		case '<': // less than operator
		case '=': // equals operator
		case '>': // greater than operator
			// case '#':
			// case '_': //underscore
		case '!':
		case '$':
		case '[':
		case '\\':
		case ']':
		case '^':
		case '{':
		case '}':
		case '~':
			return true;
		default:
			return false;
		}
	}

	private static boolean isLf(final char c) {
		return c == '\n' || c == '\r';
	}

	public abstract void scanEnd(DocumentScanner scanner);

	public abstract boolean isVariableNext();

	public abstract boolean isSqlEnable();
}