package jp.co.future.eclipse.uroborosql.plugin.contentassist.util;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jface.text.IDocument;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.IdentifierNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.SqlParser;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

public class Document {

	private final String document;
	private final int userOffset;
	private final List<Token> tokens;
	private final Token userOffsetToken;
	private final Collection<IdentifierNode> identifierNodes;

	public Document(String document) {
		this(document, 0);
	}

	public Document(String document, int userOffset) {
		this.document = document + "\n"/*dummy*/;
		this.userOffset = userOffset;
		tokens = SqlParser.parse(this);
		userOffsetToken = tokens.stream().filter(t -> t.isIn(this.userOffset - 1)).findFirst().orElse(null);
		identifierNodes = SqlParser.parseIdentifiers(tokens);
	}

	public Document(IDocument document, int userOffset) {
		this(document.get(), userOffset);
	}

	public DocumentPoint getUserOffsetDocumentPoint() {
		return new DocumentPoint(this, userOffset);
	}

	public int getUserOffset() {
		return userOffset;
	}

	public Token getUserOffsetToken() {
		return userOffsetToken;
	}

	public String getRangeText(int index) {
		if (index < userOffset) {
			return document.substring(index, userOffset);
		} else {
			return document.substring(userOffset, index);
		}
	}

	public char charAt(int index) {
		return document.charAt(index);
	}

	public String substring(int beginIndex, int endIndex) {
		return document.substring(beginIndex, endIndex);
	}

	public int length() {
		return document.length();
	}

	public Collection<IdentifierNode> getIdentifierNodes() {
		return identifierNodes;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public DocumentScanner createDocumentScanner() {
		return new DocumentScanner(this, userOffset);
	}

	public Function<String, String> getReservedCaseFormatter() {
		Token token = getUserOffsetToken();

		return Iterators.asIteratorFromNext(token, Token::getPrevToken).stream()
				.filter(prev -> prev.isReservedWord())
				.findFirst()
				.map(prev -> Optional.of(prev))
				.orElseGet(() -> {
					return getTokens().stream()
							.filter(prev -> prev.isReservedWord())
							.findFirst();
				})
				.<Function<String, String>> map(prev -> {
					return isUpperCase(prev.getString()) ? String::toUpperCase
							: isLowerCase(prev.getString()) ? String::toLowerCase : s -> s;
				})
				.orElse(s -> s);
	}

	private boolean isLowerCase(String string) {
		return string.toLowerCase().equals(string);
	}

	private boolean isUpperCase(String string) {
		return string.toUpperCase().equals(string);
	}
}
