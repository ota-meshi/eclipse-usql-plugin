package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.type;

import static jp.co.future.eclipse.uroborosql.plugin.contentassist.ContentAssistTestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.config.XmlConfig;
import jp.co.future.eclipse.uroborosql.plugin.utils.Nios;

public class StatementTypesTest {
	static XmlConfig config;

	@BeforeClass
	public static void beforeClass() throws IOException, ParserConfigurationException, SAXException {
		config = config();
	}

	@Test
	public void testSelectTable01() {

		List<String> result = computeCompletionResults("select M_M", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select M_MENU|\t-- menu data",
				"select M_MENU_BK|\t-- backup menu data",
				"select \n\tID\t\t\t\tas\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\tas\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\t\tas\tDESCRIPTION\n,\tEX_CODE1\t\tas\tEX_CODE1\t\t-- extra code #1\n,\tEX_CODE2\t\tas\tEX_CODE2\n,\tEX_CODE3\t\tas\tEX_CODE3\n,\tEX_CODE4\t\tas\tEX_CODE4\n,\tEX_CODE5\t\tas\tEX_CODE5\n,\tE_MAIL\t\t\tas\tE_MAIL\nfrom\n\tM_MENU\t-- menu data|",
				"select \n\tID\t\t\t\tas\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\tas\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\t\tas\tDESCRIPTION\nfrom\n\tM_MENU_BK\t-- backup menu data|"

		)));

		result = computeCompletionResults("SELECT /* _SQL_ID_ */ M_M", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"SELECT /* _SQL_ID_ */ M_MENU|\t-- menu data",
				"SELECT /* _SQL_ID_ */ M_MENU_BK|\t-- backup menu data",
				"SELECT /* _SQL_ID_ */ \n\tID\t\t\t\tAS\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\tAS\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\t\tAS\tDESCRIPTION\n,\tEX_CODE1\t\tAS\tEX_CODE1\t\t-- extra code #1\n,\tEX_CODE2\t\tAS\tEX_CODE2\n,\tEX_CODE3\t\tAS\tEX_CODE3\n,\tEX_CODE4\t\tAS\tEX_CODE4\n,\tEX_CODE5\t\tAS\tEX_CODE5\n,\tE_MAIL\t\t\tAS\tE_MAIL\nFROM\n\tM_MENU\t-- menu data|",
				"SELECT /* _SQL_ID_ */ \n\tID\t\t\t\tAS\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\tAS\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\t\tAS\tDESCRIPTION\nFROM\n\tM_MENU_BK\t-- backup menu data|"

		)));

		result = computeCompletionResults("SELECT c M_ME", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"SELECT c M_MENU|\t-- menu data",
				"SELECT c M_MENU_BK|\t-- backup menu data",
				"SELECT c MST_MENU|\t-- メニューマスタ")));
	}

	@Test
	public void testSelectCols01() {

		List<String> result = computeCompletionResults("select m.E| from M_MENU m", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select m.EX_CODE1\tas\t|EX_CODE1\t-- extra code #1\n from M_MENU m",
				"select m.EX_CODE1|\t-- extra code #1\n from M_MENU m",
				"select m.EX_CODE2| from M_MENU m",
				"select m.EX_CODE2\tas\t|EX_CODE2 from M_MENU m",
				"select m.EX_CODE3| from M_MENU m",
				"select m.EX_CODE3\tas\t|EX_CODE3 from M_MENU m",
				"select m.EX_CODE4| from M_MENU m",
				"select m.EX_CODE4\tas\t|EX_CODE4 from M_MENU m",
				"select m.EX_CODE5| from M_MENU m",
				"select m.EX_CODE5\tas\t|EX_CODE5 from M_MENU m",
				"select m.E_MAIL| from M_MENU m",
				"select m.E_MAIL\t\tas\t|E_MAIL from M_MENU m"

		)));
	}

	@Test
	public void testSelectCols02() {

		List<String> result = computeCompletionResults("select u.| from M_USER u", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select u.E_MAIL\t\tas\t|E_MAIL\t\t-- e-mail addr\n from M_USER u",
				"select u.E_MAIL|\t-- e-mail addr\n from M_USER u",
				"select u.F_NAME\t\tas\t|F_NAME\t\t-- user first name\n from M_USER u",
				"select u.F_NAME|\t-- user first name\n from M_USER u",
				"select u.ID\t\tas\t|ID\t\t-- user id\n from M_USER u",
				"select u.ID|\t-- user id\n from M_USER u",
				"select u.L_NAME\t\tas\t|L_NAME\t\t-- user last name\n from M_USER u",
				"select u.L_NAME|\t-- user last name\n from M_USER u"

		)));
	}

	@Test
	public void testFromTable() {

		List<String> result = computeCompletionResults("select * from M_ME", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select * from M_MENU|\t-- menu data",
				"select * from M_MENU_BK|\t-- backup menu data",
				"select * from MST_MENU|\t-- メニューマスタ"

		)));

		result = computeCompletionResults("SELECT * FROM M_ME", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"SELECT * FROM M_MENU|\t-- menu data",
				"SELECT * FROM M_MENU_BK|\t-- backup menu data",
				"SELECT * FROM MST_MENU|\t-- メニューマスタ"

		)));
	}

	@Test
	public void testFromCols() {

		List<String> result = computeCompletionResults("select * "
				+ "from M_MENU m "
				+ "left join m_user u "
				+ "on u.f", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select * from M_MENU m left join m_user u on u.F_NAME|\t-- user first name")));

		result = computeCompletionResults("select * "
				+ "from M_MENU m "
				+ "left join m_user u "
				+ "on u.", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select * from M_MENU m left join m_user u on u.E_MAIL|\t-- e-mail addr",
				"select * from M_MENU m left join m_user u on u.F_NAME|\t-- user first name",
				"select * from M_MENU m left join m_user u on u.ID|\t-- user id",
				"select * from M_MENU m left join m_user u on u.L_NAME|\t-- user last name"

		)));
	}

	@Test
	public void testWhereCols01() {

		List<String> result = computeCompletionResults("select * from M_MENU m where m.E", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select * from M_MENU m where m.EX_CODE1|\t-- extra code #1",
				"select * from M_MENU m where m.EX_CODE1\t=\t/*|exCode1*/''\t\t-- extra code #1",
				"select * from M_MENU m where m.EX_CODE2|",
				"select * from M_MENU m where m.EX_CODE2\t=\t/*|exCode2*/''",
				"select * from M_MENU m where m.EX_CODE3|",
				"select * from M_MENU m where m.EX_CODE3\t=\t/*|exCode3*/''",
				"select * from M_MENU m where m.EX_CODE4|",
				"select * from M_MENU m where m.EX_CODE4\t=\t/*|exCode4*/''",
				"select * from M_MENU m where m.EX_CODE5|",
				"select * from M_MENU m where m.EX_CODE5\t=\t/*|exCode5*/''",
				"select * from M_MENU m where m.E_MAIL|",
				"select * from M_MENU m where m.E_MAIL\t\t=\t/*|eMail*/''"

		)));
	}

	@Test
	public void testWhereCols02() {

		List<String> result = computeCompletionResults("select * from m_user m where m.| ", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select * from m_user m where m.E_MAIL|\t-- e-mail addr\n ",
				"select * from m_user m where m.E_MAIL\t\t=\t/*|eMail*/''\t\t-- e-mail addr\n ",
				"select * from m_user m where m.F_NAME|\t-- user first name\n ",
				"select * from m_user m where m.F_NAME\t\t=\t/*|fName*/''\t\t-- user first name\n ",
				"select * from m_user m where m.ID|\t-- user id\n ",
				"select * from m_user m where m.ID\t\t=\t/*|id*/''\t-- user id\n ",
				"select * from m_user m where m.L_NAME|\t-- user last name\n ",
				"select * from m_user m where m.L_NAME\t\t=\t/*|lName*/''\t\t-- user last name\n "

		)));
	}

	@Test
	public void testUpdateTable() {

		List<String> result = computeCompletionResults("update M_M", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"update M_MENU|\t-- menu data",
				"update M_MENU_BK|\t-- backup menu data",
				"update \n\tM_MENU\t-- menu data\nset\n\tID\t\t\t\t=\t/*id*/''\t\t\t\t-- menu id\n,\tCAPTION\t\t\t=\t/*caption*/''\t\t\t-- menu caption\n,\tDESCRIPTION\t\t=\t/*description*/''\n,\tEX_CODE1\t\t=\t/*exCode1*/''\t\t\t-- extra code #1\n,\tEX_CODE2\t\t=\t/*exCode2*/''\n,\tEX_CODE3\t\t=\t/*exCode3*/''\n,\tEX_CODE4\t\t=\t/*exCode4*/''\n,\tEX_CODE5\t\t=\t/*exCode5*/''\n,\tE_MAIL\t\t\t=\t/*eMail*/''|",
				"update \n\tM_MENU_BK\t-- backup menu data\nset\n\tID\t\t\t\t=\t/*id*/''\t\t\t\t-- menu id\n,\tCAPTION\t\t\t=\t/*caption*/''\t\t\t-- menu caption\n,\tDESCRIPTION\t\t=\t/*description*/''|"

		)));
	}

	@Test
	public void testUpdateCols01() {

		List<String> result = computeCompletionResults("update M_MENU set E", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"update M_MENU set EX_CODE1|\t-- extra code #1",
				"update M_MENU set EX_CODE1\t=\t/*|exCode1*/''\t\t-- extra code #1",
				"update M_MENU set EX_CODE2|",
				"update M_MENU set EX_CODE2\t=\t/*|exCode2*/''",
				"update M_MENU set EX_CODE3|",
				"update M_MENU set EX_CODE3\t=\t/*|exCode3*/''",
				"update M_MENU set EX_CODE4|",
				"update M_MENU set EX_CODE4\t=\t/*|exCode4*/''",
				"update M_MENU set EX_CODE5|",
				"update M_MENU set EX_CODE5\t=\t/*|exCode5*/''",
				"update M_MENU set E_MAIL|",
				"update M_MENU set E_MAIL\t\t=\t/*|eMail*/''"

		)));

		result = computeCompletionResults("update M_MENU set u.E_MA| where M_USER u", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"update M_MENU set u.E_MAIL|\t-- e-mail addr\n where M_USER u",
				"update M_MENU set u.E_MAIL\t\t=\t/*|eMail*/''\t\t-- e-mail addr\n where M_USER u"

		)));
	}

	@Test
	public void testUpdateCols02() {

		List<String> result = computeCompletionResults("update M_MENU set u.| where M_USER u", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"update M_MENU set u.E_MAIL|\t-- e-mail addr\n where M_USER u",
				"update M_MENU set u.E_MAIL\t\t=\t/*|eMail*/''\t\t-- e-mail addr\n where M_USER u",
				"update M_MENU set u.F_NAME|\t-- user first name\n where M_USER u",
				"update M_MENU set u.F_NAME\t\t=\t/*|fName*/''\t\t-- user first name\n where M_USER u",
				"update M_MENU set u.ID|\t-- user id\n where M_USER u",
				"update M_MENU set u.ID\t\t=\t/*|id*/''\t-- user id\n where M_USER u",
				"update M_MENU set u.L_NAME|\t-- user last name\n where M_USER u",
				"update M_MENU set u.L_NAME\t\t=\t/*|lName*/''\t\t-- user last name\n where M_USER u")));
	}

	@Test
	public void testInsertTable01() {

		List<String> result = computeCompletionResults("insert /**/ into M_M", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert /**/ into M_MENU|\t-- menu data",
				"insert /**/ into M_MENU_BK|\t-- backup menu data",
				"insert /**/ into \n\tM_MENU\t-- menu data\n(\n\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\n,\tEX_CODE1\t\t-- extra code #1\n,\tEX_CODE2\n,\tEX_CODE3\n,\tEX_CODE4\n,\tEX_CODE5\n,\tE_MAIL\n)|",
				"insert /**/ into \n\tM_MENU_BK\t-- backup menu data\n(\n\tID\t\t\t\t-- menu id\n,\tCAPTION\t\t\t-- menu caption\n,\tDESCRIPTION\n)|"

		)));
	}

	@Test
	public void testInsertTable02() {

		List<String> result = computeCompletionResults("insert /**/ into メニ", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(

				"insert /**/ into MST_MENU|\t-- メニューマスタ",
				"insert /**/ into \n\tMST_MENU\t-- メニューマスタ\n(\n\tID\n,\tCAPTION\n,\tDESCRIPTION\n)|"

		)));
	}

	@Test
	public void testInsertCols() {

		List<String> result = computeCompletionResults("insert into M_MENU (E", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_MENU (EX_CODE1|\t-- extra code #1",
				"insert into M_MENU (EX_CODE1|\t-- extra code #1",
				"insert into M_MENU (EX_CODE2|",
				"insert into M_MENU (EX_CODE2|",
				"insert into M_MENU (EX_CODE3|",
				"insert into M_MENU (EX_CODE3|",
				"insert into M_MENU (EX_CODE4|",
				"insert into M_MENU (EX_CODE4|",
				"insert into M_MENU (EX_CODE5|",
				"insert into M_MENU (EX_CODE5|",
				"insert into M_MENU (E_MAIL|",
				"insert into M_MENU (E_MAIL|"

		)));
	}

	@Test
	public void testInsertValuesCols01() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VALUES ( ", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) VALUES ( /*id*/''|"

		)));

	}

	@Test
	public void testInsertValuesCols02() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VALUES (/*id*/'', |)", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) VALUES (/*id*/'', /*caption*/''|)"

		)));
	}

	@Test
	public void testInsertValuesCols03() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VALUES (/*id*/'',(( |)",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) VALUES (/*id*/'',(( /*caption*/''|)"

		)));
	}

	@Test
	public void testInsertValuesCols04() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VALUES (/*id*/''\n,\t",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) VALUES (/*id*/''\n,\t/*caption*/''|")));
	}

	@Test
	public void testInsertValuesCols05() {

		List<String> result = computeCompletionResults(
				"insert into M_USER (ID, CAPTION -- cap\n) VALUES (/*id*/''\n,\t",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION -- cap\n) VALUES (/*id*/''\n,\t/*caption*/''\t\t-- cap|"

		)));
	}

	@Test
	public void testInsertValuesToken01() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VALUES",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) values (\n\t/*id*/''\n,\t/*caption*/''\n)|"

		)));
	}

	@Test
	public void testInsertValuesToken02() {

		List<String> result = computeCompletionResults("insert into M_USER (ID, CAPTION) VAL|UE",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID, CAPTION) values (\n\t/*id*/''\n,\t/*caption*/''\n)|"

		)));
	}

	@Test
	public void testInsertValuesToken03() {

		List<String> result = computeCompletionResults("insert into M_USER (ID\t-- ID\n, CAPTION\t-- キャプション\n) VAL|UE",
				config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"insert into M_USER (ID\t-- ID\n, CAPTION\t-- キャプション\n) values (\n\t/*id*/''\t\t\t-- ID\n,\t/*caption*/''\t\t-- キャプション\n)|"

		)));
	}

	private static XmlConfig config() throws IOException, ParserConfigurationException, SAXException {
		XmlConfig config = new XmlConfig(xml().getBytes(StandardCharsets.UTF_8), null);

		config.sql(
				"CREATE TABLE M_MENU (ID VARCHAR(10),CAPTION VARCHAR(30),DESCRIPTION VARCHAR(30),EX_CODE1 VARCHAR(10),EX_CODE2 VARCHAR(10),EX_CODE3 VARCHAR(10),EX_CODE4 VARCHAR(10),EX_CODE5 VARCHAR(10),E_MAIL VARCHAR(120))");
		config.sql("CREATE TABLE M_MENU_BK (ID VARCHAR(10),CAPTION VARCHAR(30),DESCRIPTION VARCHAR(30))");
		config.sql("CREATE TABLE M_USER (ID VARCHAR(10),F_NAME VARCHAR(30),L_NAME VARCHAR(30),E_MAIL VARCHAR(120))");
		config.sql("CREATE TABLE MST_MENU (ID VARCHAR(10),CAPTION VARCHAR(30),DESCRIPTION VARCHAR(30))");

		config.sql("COMMENT ON TABLE M_MENU IS 'menu data'");
		config.sql("COMMENT ON TABLE M_MENU_BK IS 'backup menu data'");
		config.sql("COMMENT ON TABLE M_USER IS 'user data'");
		config.sql("COMMENT ON COLUMN M_MENU.ID IS 'menu id'");
		config.sql("COMMENT ON COLUMN M_MENU.CAPTION IS 'menu caption'");

		config.sql("COMMENT ON COLUMN M_MENU.EX_CODE1 IS 'extra code #1'");

		config.sql("COMMENT ON COLUMN M_MENU_BK.ID IS 'menu id'");
		config.sql("COMMENT ON COLUMN M_MENU_BK.CAPTION IS 'menu caption'");
		config.sql("COMMENT ON COLUMN M_USER.ID IS 'user id'");
		config.sql("COMMENT ON COLUMN M_USER.F_NAME IS 'user first name'");
		config.sql("COMMENT ON COLUMN M_USER.L_NAME IS 'user last name'");
		config.sql("COMMENT ON COLUMN M_USER.E_MAIL IS 'e-mail addr'");

		config.sql("COMMENT ON TABLE MST_MENU IS 'メニューマスタ'");

		return config;
	}

	private static String xml() throws IOException {
		Path dbfile = Paths.get("./").toRealPath().resolve("testdb").resolve(StatementTypesTest.class.getSimpleName())
				.resolve("database");

		Nios.deleteDirectories(dbfile.getParent());
		Nios.createDirectories(dbfile.getParent());

		return "<config>"
				+ "	<contentassist>" +
				"		<tables>" +
				"			<sql>" +
				"				SELECT\r\n" +
				"				 	TBL.TABLE_NAME  AS TABLE_NAME\r\n" +
				"				,	TBL.REMARKS     AS COMMENTS\r\n" +
				"				FROM\r\n" +
				"					INFORMATION_SCHEMA.TABLES   AS TBL\r\n" +
				"				WHERE\r\n" +
				"					TBL.TABLE_SCHEMA	=       SCHEMA()\r\n" +
				"				AND	TBL.TABLE_NAME		LIKE	/*TABLE_NAME*/'tableName' || '%'\r\n" +
				"				ORDER BY\r\n" +
				"				    TBL.TABLE_NAME" +
				"			</sql>" +
				"			<lazySql>" +
				"				SELECT\r\n" +
				"				 	TBL.TABLE_NAME  AS TABLE_NAME\r\n" +
				"				,	TBL.REMARKS     AS COMMENTS\r\n" +
				"				FROM\r\n" +
				"					INFORMATION_SCHEMA.TABLES   AS TBL\r\n" +
				"				WHERE\r\n" +
				"					TBL.TABLE_SCHEMA	=       SCHEMA()\r\n" +
				"				AND	(TBL.TABLE_NAME		LIKE	'%' || /*TABLE_NAME*/'tableName' || '%'\r\n" +
				"					OR	TBL.REMARKS			LIKE	'%' || /*tableName*/'tableName' || '%'\r\n" +
				"					)\r\n" +
				"				ORDER BY\r\n" +
				"				    TBL.TABLE_NAME" +
				"			</lazySql>" +
				"		</tables>" +
				"		<columns>" +
				"			<sql>" +
				"				SELECT\r\n" +
				"				    COL.COLUMN_NAME                 AS COLUMN_NAME\r\n" +
				"				,   COL.REMARKS                     AS COMMENTS\r\n" +
				//				"				,   COL.TYPE_NAME                   AS DATA_TYPE\r\n" +
				//				"				,   COL.CHARACTER_MAXIMUM_LENGTH    AS DATA_LENGTH\r\n" +
				//				"				,   COL.NUMERIC_PRECISION           AS DATA_PRECISION\r\n" +
				//				"				,   COL.NUMERIC_SCALE               AS DATA_SCALE\r\n" +
				//				"				,   COL.IS_NULLABLE                 AS NULLABLE\r\n" +
				"				,   COL.COLUMN_DEFAULT              AS DATA_DEFAULT\r\n" +
				"				FROM\r\n" +
				"				    INFORMATION_SCHEMA.COLUMNS  COL\r\n" +
				"				WHERE\r\n" +
				"				    COL.TABLE_SCHEMA    = SCHEMA()\r\n" +
				"				AND COL.TABLE_NAME      = /*tableName*/'TABLE_NAME'\r\n" +
				"				ORDER BY\r\n" +
				"				    COL.TABLE_NAME\r\n" +
				"				,   COL.ORDINAL_POSITION" +
				"			</sql>" +
				"		</columns>" +
				"	</contentassist>"
				+ "	<db>"
				+ "		<url>jdbc:h2:" + dbfile.toUri() + "</url>"
				+ "		<user>sa</user>"
				+ "		<classpath>" + Paths.get("testlib/h2-1.4.196.jar").toRealPath().toString() + "</classpath>"
				+ "	</db>"
				+ "</config>";
	}

}
