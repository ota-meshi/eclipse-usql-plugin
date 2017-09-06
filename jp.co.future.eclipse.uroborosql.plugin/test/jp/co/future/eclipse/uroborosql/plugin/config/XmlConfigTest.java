package jp.co.future.eclipse.uroborosql.plugin.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.M;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.PrintMap;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.Const;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables.IVariable;

public class XmlConfigTest {
	static class Consts extends PrintMap<String, IVariable> {

		public Consts(Map<String, IVariable> map) {
			super(map);
		}

		@Override
		protected String[] toString(String k, IVariable v) {
			return new String[] { TestUtil.stringLiteral(k),
					toString(v)
			};
		}

		private String toString(IVariable v) {
			String[] ss = v.getDescription() != null
					? new String[] { v.getVariableName(), v.getSqlValue(), v.getDescription() }
					: v.getSqlValue() != null ? new String[] { v.getVariableName(), v.getSqlValue() }
							: new String[] { v.getVariableName() };

			return "new " + v.getClass().getSimpleName() + "(" +
					Arrays.stream(ss).map(TestUtil::stringLiteral).collect(Collectors.joining(", ")) +
					")";
		}
	}

	@Test
	public void test() throws IOException, ParserConfigurationException, SAXException {
		XmlConfig config = new XmlConfig(xml().getBytes(StandardCharsets.UTF_8), null);

		assertThat(config.getSqlId(), is("_TEST_ID_"));

		assertThat(new Consts(config.getConsts().asMap()),
				is(

						new M<>("CLS_TEST_ZONE_OFFSET", new Const("CLS_TEST_ZONE_OFFSET", "15"))
								.p("CLS_TEST_HOUR_OF_DAY", new Const("CLS_TEST_HOUR_OF_DAY", "11"))
								.p("CLS_TEST_SHORT_FORMAT", new Const("CLS_TEST_SHORT_FORMAT", "1"))
								.p("CLS_TEST_SATURDAY", new Const("CLS_TEST_SATURDAY", "7"))
								.p("CLS_TEST_WEEK_OF_MONTH", new Const("CLS_TEST_WEEK_OF_MONTH", "4"))
								.p("CLS_TEST_AM_PM", new Const("CLS_TEST_AM_PM", "9"))
								.p("CLS_TEST_SHORT_STANDALONE", new Const("CLS_TEST_SHORT_STANDALONE", "32769"))
								.p("CLS_TEST_HOUR", new Const("CLS_TEST_HOUR", "10"))
								.p("CLS_TEST_SUNDAY", new Const("CLS_TEST_SUNDAY", "1"))
								.p("CLS_TEST_SHORT", new Const("CLS_TEST_SHORT", "1"))
								.p("CLS_TEST_APRIL", new Const("CLS_TEST_APRIL", "3"))
								.p("CLS_TEST_FEBRUARY", new Const("CLS_TEST_FEBRUARY", "1"))
								.p("CLS_TEST_SECOND", new Const("CLS_TEST_SECOND", "13"))
								.p("CLS_TEST_YEAR", new Const("CLS_TEST_YEAR", "1"))
								.p("CLS_TEST_AUGUST", new Const("CLS_TEST_AUGUST", "7"))
								.p("CLS_TEST_WEEK_OF_YEAR", new Const("CLS_TEST_WEEK_OF_YEAR", "3"))
								.p("CLS_TEST_DATE", new Const("CLS_TEST_DATE", "5"))
								.p("CLS_TEST_AM", new Const("CLS_TEST_AM", "0"))
								.p("CLS_TEST_TUESDAY", new Const("CLS_TEST_TUESDAY", "3"))
								.p("CLS_TEST_JUNE", new Const("CLS_TEST_JUNE", "5"))
								.p("CLS_TEST_MONTH", new Const("CLS_TEST_MONTH", "2"))
								.p("CLS_TEST_JANUARY", new Const("CLS_TEST_JANUARY", "0"))
								.p("CLS_TEST_DAY_OF_WEEK", new Const("CLS_TEST_DAY_OF_WEEK", "7"))
								.p("CLS_TEST_LONG_STANDALONE", new Const("CLS_TEST_LONG_STANDALONE", "32770"))
								.p("CLS_TEST_MINUTE", new Const("CLS_TEST_MINUTE", "12"))
								.p("CLS_TEST_DAY_OF_WEEK_IN_MONTH", new Const("CLS_TEST_DAY_OF_WEEK_IN_MONTH", "8"))
								.p("CLS_TEST_FIELD_COUNT", new Const("CLS_TEST_FIELD_COUNT", "17"))
								.p("CLS_TEST_UNDECIMBER", new Const("CLS_TEST_UNDECIMBER", "12"))
								.p("CLS_TEST_ALL_STYLES", new Const("CLS_TEST_ALL_STYLES", "0"))
								.p("CLS_TEST_MONDAY", new Const("CLS_TEST_MONDAY", "2"))
								.p("CLS_TEST_DST_OFFSET", new Const("CLS_TEST_DST_OFFSET", "16"))
								.p("CLS_TEST_DAY_OF_MONTH", new Const("CLS_TEST_DAY_OF_MONTH", "5"))
								.p("CLS_TEST_MAY", new Const("CLS_TEST_MAY", "4"))
								.p("CLS_TEST_LONG_FORMAT", new Const("CLS_TEST_LONG_FORMAT", "2"))
								.p("CLS_TEST_NOVEMBER", new Const("CLS_TEST_NOVEMBER", "10"))
								.p("CLS_TEST_JULY", new Const("CLS_TEST_JULY", "6"))
								.p("CLS_TEST_THURSDAY", new Const("CLS_TEST_THURSDAY", "5"))
								.p("DB_DATA2", new Const("DB_DATA2", "'test value'"))
								.p("CLS_TEST_DECEMBER", new Const("CLS_TEST_DECEMBER", "11"))
								.p("CLS_TEST_WEDNESDAY", new Const("CLS_TEST_WEDNESDAY", "4"))
								.p("CLS_TEST_OCTOBER", new Const("CLS_TEST_OCTOBER", "9"))
								.p("DB_DATA", new Const("DB_DATA", "'test value'", "description"))
								.p("CLS_TEST_FRIDAY", new Const("CLS_TEST_FRIDAY", "6"))
								.p("CLS_TEST_ERA", new Const("CLS_TEST_ERA", "0"))
								.p("DB_DATA3", new Const("DB_DATA3"))
								.p("CLS_TEST_SEPTEMBER", new Const("CLS_TEST_SEPTEMBER", "8"))
								.p("CLS_TEST_NARROW_STANDALONE", new Const("CLS_TEST_NARROW_STANDALONE", "32772"))
								.p("CLS_TEST_MARCH", new Const("CLS_TEST_MARCH", "2"))
								.p("CLS_TEST_NARROW_FORMAT", new Const("CLS_TEST_NARROW_FORMAT", "4"))
								.p("CLS_TEST_DAY_OF_YEAR", new Const("CLS_TEST_DAY_OF_YEAR", "6"))
								.p("CLS_TEST_MILLISECOND", new Const("CLS_TEST_MILLISECOND", "14"))
								.p("CLS_TEST_LONG", new Const("CLS_TEST_LONG", "2"))
								.p("CLS_TEST_PM", new Const("CLS_TEST_PM", "1"))

				));
	}

	private String xml() throws IOException {

		return "<config>"
				+ "	<sqlid>_TEST_ID_</sqlid>"
				+ "	<sqlContextFactory>"
				+ "		<constParamPrefix>CLS_TEST_</constParamPrefix>"
				+ "		<constantClassName>" + Calendar.class.getName() + "</constantClassName>"
				+ "		<constantSqls>"
				+ "			<value>select 'DB_DATA','test value','description' from dual</value>"
				+ "			<value>select 'DB_DATA2','test value' from dual</value>"
				+ "			<value>select 'DB_DATA3' from dual</value>"
				+ "		</constantSqls>"
				+ "	</sqlContextFactory>"
				+ "	<db>"
				+ "		<url>jdbc:h2:mem:" + this.getClass().getSimpleName() + "</url>"
				+ "		<user>sa</user>"
				+ "		<classpath>" + Paths.get("testlib/h2-1.4.196.jar").toRealPath().toString() + "</classpath>"
				+ "	</db>"
				+ "</config>";
	}

}
