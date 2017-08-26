package jp.co.future.eclipse.uroborosql.plugin.config.xml;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.TestUtil.StringList;

public class ITargetTest {

	@Test
	public void testValue() throws ParserConfigurationException, SAXException, IOException {
		Document document =

				Xml.parse("<root>"
						+ "	<sql>value</sql>"
						+ "	<sql2 value=\"value2\" />"
						+ "</root>");
		Element root = document.getDocumentElement();

		assertThat(ITarget.get(root, "sql").value().get(), is("value"));
		assertThat(ITarget.get(root, "sql2").value().get(), is("value2"));

		assertThat(ITarget.get(root, "notfound").value(), is(Optional.empty()));

	}

	@Test
	public void testValues() throws ParserConfigurationException, SAXException, IOException {
		Document document =

				Xml.parse("<root nontgt=\"aaa\">"
						+ "	<sqls>"
						+ "		<value>value11</value>"
						+ "		<value>value12</value>"
						+ "	</sqls>"
						+ "	<sql2>value21</sql2>"
						+ "	<sql2>value22</sql2>"
						+ "	<sql3>value3</sql3>"
						+ "	<sql4 value=\"value4\" />"
						+ "	<sql5s value=\"value51, value52\" />"
						+ "	<sql6s>"
						+ "		value61,"
						+ "		value62\r\n"
						+ "		value63,"
						+ "	</sql6s>"
						+ "	<tag sql7s=\"value71, value72\"/>"
						+ "</root>");
		Element root = document.getDocumentElement();

		assertThat(ITarget.get(root, "sql").values(), is(Arrays.asList("value11", "value12")));
		assertThat(ITarget.get(root, "sql2").values(), is(Arrays.asList("value21", "value22")));
		assertThat(ITarget.get(root, "sql3").values(), is(Arrays.asList("value3")));
		assertThat(ITarget.get(root, "sql4").values(), is(Arrays.asList("value4")));
		assertThat(new StringList(ITarget.get(root, "sql5").values()), is(Arrays.asList("value51", "value52")));
		assertThat(new StringList(ITarget.get(root, "sql6").values()),
				is(Arrays.asList("value61", "value62", "value63")));

		assertThat(new StringList(ITarget.get(root, "tag", "sql7").values()), is(Arrays.asList("value71", "value72")));
		assertThat(new StringList(ITarget.get(root, "taggggg", "nontgt").values()),
				is(Collections.emptyList()));

		assertThat(ITarget.get(root, "notfound").values(), is(Collections.emptyList()));

	}

	@Test
	public void testChain() throws ParserConfigurationException, SAXException, IOException {
		Document document =

				Xml.parse("<root>"
						+ "	<c1>"
						+ "		<c2>"
						+ "			<c3>"
						+ "				<tgt>v</tgt>"
						+ "			</c3>"
						+ "		</c2>"
						+ "	</c1>"
						+ "</root>");
		Element root = document.getDocumentElement();

		assertThat(ITarget.get(root, "c1", "c2", "c3", "tgt").value().get(), is("v"));
		assertThat(ITarget.get(root, "c1", "c2", "tgt").value(), is(Optional.empty()));
		assertThat(ITarget.get(root, "c1", "c2", "c3", "tgt", "no").value(), is(Optional.empty()));

	}

	@Test
	public void testIgnoreCase() throws ParserConfigurationException, SAXException, IOException {
		Document document =

				Xml.parse("<ROOT>"
						+ "	<C1>"
						+ "		<C2>"
						+ "			<C3>"
						+ "				<TGT>v</TGT>"
						+ "			</C3>"
						+ "		</C2>"
						+ "	</C1>"
						+ "</ROOT>");
		Element root = document.getDocumentElement();

		assertThat(ITarget.get(root, "c1", "c2", "c3", "tgt").value().get(), is("v"));

	}
}
