package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

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

public class ColumnsTest {
	static XmlConfig config;

	@BeforeClass
	public static void beforeClass() throws IOException, ParserConfigurationException, SAXException {
		config = config();
	}

	@Test
	public void testAdd() {

		List<String> result = computeCompletionResults("select M_M", config);
		result.forEach(System.out::println);

		assertThat(result, is(Arrays.asList(
				"select M_MENU|\t-- menu data",
				"select \n\tID\t\t\tas\tID\t\t\t-- menu id\n,\tCAPTION\t\tas\tCAPTION\t\t-- menu caption\n,\tE_MAIL\t\tas\tE_MAIL\t\t-- mail addr\nfrom\n\tM_MENU\t-- menu data|"

		)));

	}

	private static XmlConfig config() throws IOException, ParserConfigurationException, SAXException {
		XmlConfig config = new XmlConfig(xml().getBytes(StandardCharsets.UTF_8), null);

		config.sql(
				"CREATE TABLE M_MENU (ID VARCHAR(10),CAPTION VARCHAR(30),E_MAIL VARCHAR(120))");

		config.sql("COMMENT ON TABLE M_MENU IS 'menu data'");
		config.sql("COMMENT ON COLUMN M_MENU.ID IS 'menu id'");
		config.sql("COMMENT ON COLUMN M_MENU.CAPTION IS 'menu caption'");

		config.sql("COMMENT ON COLUMN M_MENU.E_MAIL IS 'mail addr'");

		return config;
	}

	private static String xml() throws IOException {
		Path dbfile = Paths.get("./").toRealPath().resolve("testdb").resolve(ColumnsTest.class.getSimpleName())
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
				"			<sql>" +
				"				SELECT\r\n" +
				"				 	TBL.TABLE_NAME  AS TABLE_NAME\r\n" +
				"				,	TBL.REMARKS || ' error'     AS COMMENTS\r\n" +
				"				FROM\r\n" +
				"					INFORMATION_SCHEMA.TABLES   AS TBL\r\n" +
				"				WHERE\r\n" +
				"					TBL.TABLE_SCHEMA	=       SCHEMA()\r\n" +
				"				AND	TBL.TABLE_NAME		LIKE	/*TABLE_NAME*/'tableName' || '%'\r\n" +
				"				ORDER BY\r\n" +
				"				    TBL.TABLE_NAME" +
				"			</sql>" +
				"		</tables>" +
				"		<columns>" +
				"			<sql>" +
				"				SELECT\r\n" +
				"				    COL.COLUMN_NAME                 AS COLUMN_NAME\r\n" +
				"				,   COL.REMARKS                     AS COMMENTS\r\n" +
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
				"			<sql>" +
				"				SELECT\r\n" +
				"				    COL.COLUMN_NAME                 AS COLUMN_NAME\r\n" +
				"				,   COL.REMARKS      || ' error'    AS COMMENTS\r\n" +
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
