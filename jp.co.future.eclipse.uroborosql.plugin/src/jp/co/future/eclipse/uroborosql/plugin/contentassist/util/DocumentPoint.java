package jp.co.future.eclipse.uroborosql.plugin.contentassist.util;

import java.util.Optional;
import java.util.function.Function;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser.Token;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Iterators;

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

	public Function<String, String> getReservedCaseFormatter() {
		Token token = getDocument().getUserOffsetToken();

		return Iterators.asIteratorFromNext(token, Token::getPrevToken).stream()
				.filter(prev -> prev.isReservedWord())
				.findFirst()
				.map(prev -> Optional.of(prev))
				.orElseGet(() -> {
					return getDocument().getTokens().stream()
							.filter(prev -> prev.isReservedWord())
							.findFirst();
				})
				.<Function<String, String>> map(prev -> {
					return isUpperCase(prev.getString()) ? String::toUpperCase
							: isLowerCase(prev.getString()) ? String::toLowerCase : s -> s;
				})
				.orElse(s -> s);
	}

	public Token getToken() {
		return getDocument().getTokens().stream()
				.filter(t -> t.isIn(point))
				.findFirst().orElse(null);
	}

	private boolean isLowerCase(String string) {
		return string.toLowerCase().equals(string);
	}

	private boolean isUpperCase(String string) {
		return string.toUpperCase().equals(string);
	}
}