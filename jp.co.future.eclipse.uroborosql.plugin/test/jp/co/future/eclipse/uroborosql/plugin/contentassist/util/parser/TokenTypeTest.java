package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.PrintList;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.StringList;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.Document;

public class TokenTypeTest {

	@Test
	public void testSYMBOL() {
		List<Token> tokens = parse(
				".+");
		assertThat(strings(tokens), is(Arrays.asList(
				".", "+", "\n")));
		assertThat(types(tokens), is(Arrays.asList(
				TokenType.SYMBOL, TokenType.SYMBOL, TokenType.WHITESPACE

		)));
	}

	private List<Token> parse(String text) {
		return new Document(TestUtil.createDocument(text), 0).getTokens();
	}

	private List<String> strings(List<Token> tokens) {
		return tokens.stream().map(t -> t.getString()).collect(Collectors.toCollection(StringList::new))
				.setPrintLines(false);
	}

	private List<TokenType> types(List<Token> tokens) {
		class PrintListImpl extends PrintList<TokenType> {
			@Override
			protected String toString(TokenType e) {
				return "TokenType." + e;
			}
		}

		return tokens.stream().map(t -> t.getType()).collect(Collectors.toCollection(PrintListImpl::new))
				.setPrintLines(false);
	}
}
