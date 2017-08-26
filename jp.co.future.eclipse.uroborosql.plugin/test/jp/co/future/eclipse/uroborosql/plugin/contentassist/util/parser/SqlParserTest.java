package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.parser;

import static jp.co.future.eclipse.uroborosql.plugin.contentassist.ContentAssistTestUtil.*;
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

public class SqlParserTest {

	@Test
	public void testComputeCompletionProposals05() {
		List<String> result = computeCompletionResults("select a.column1,a.column2 from user_table a where /*");

		assertThat(result, is(Arrays.asList(
				"select a.column1,a.column2 from user_table a where /*column1*/''|",
				"select a.column1,a.column2 from user_table a where /*column2*/''|",
				"select a.column1,a.column2 from user_table a where /*userTable*/''|",
				"select a.column1,a.column2 from user_table a where /*BEGIN*/\n|/*END*/",
				"select a.column1,a.column2 from user_table a where /*ELIF |*/",
				"select a.column1,a.column2 from user_table a where /*ELSE*/|",
				"select a.column1,a.column2 from user_table a where /*END*/|",
				"select a.column1,a.column2 from user_table a where /*IF |*/\n/*END*/",
				"select a.column1,a.column2 from user_table a where /* _SQL_ID_ */|")));

	}

	@Test
	public void testParse() {
		List<Token> tokens = parse(
				"select\n"
						+ "	col.table_name	/* table_name */\n"
						+ ",	col.column_name	-- colname\n"
						+ ",	'lit' as lt\n"
						+ "from\n"
						+ "	tab_columns col\n"
						+ "where\n"
						+ "	1	=	1\n"
						+ "and	col.column_id	=	1234\r\n"
						+ "and \"data\" is not null");
		assertThat(strings(tokens), is(Arrays.asList(
				"select", "\n\t", "col", ".", "table_name", "\t", "/* table_name */", "\n", ",", "\t", "col", ".",
				"column_name", "\t", "-- colname\n", ",", "\t", "'lit'", " ", "as", " ", "lt", "\n", "from", "\n\t",
				"tab_columns", " ", "col", "\n", "where", "\n\t", "1", "\t", "=", "\t", "1", "\n", "and", "\t", "col",
				".", "column_id", "\t", "=", "\t", "1234", "\r\n", "and", " ", "\"data\"", " ", "is", " ", "not", " ",
				"null", "\n"

		)));
		assertThat(types(tokens), is(Arrays.asList(
				TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.SYMBOL, TokenType.SQL_TOKEN,
				TokenType.WHITESPACE, TokenType.M_COMMENT, TokenType.WHITESPACE, TokenType.SYMBOL, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.SYMBOL, TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.L_COMMENT,
				TokenType.SYMBOL, TokenType.WHITESPACE, TokenType.STR_LATERAL, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE,
				TokenType.NUM_LATERAL, TokenType.WHITESPACE, TokenType.SYMBOL, TokenType.WHITESPACE,
				TokenType.NUM_LATERAL, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.SYMBOL, TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SYMBOL,
				TokenType.WHITESPACE, TokenType.NUM_LATERAL, TokenType.WHITESPACE, TokenType.SQL_TOKEN,
				TokenType.WHITESPACE, TokenType.NAME, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE,
				TokenType.SQL_TOKEN, TokenType.WHITESPACE, TokenType.SQL_TOKEN, TokenType.WHITESPACE

		)));

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

	private List<Token> parse(String text) {
		return new Document(TestUtil.createDocument(text), 0).getTokens();
	}

}
