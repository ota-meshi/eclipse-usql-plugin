package jp.co.future.eclipse.uroborosql.plugin.contentassist;

import static jp.co.future.eclipse.uroborosql.plugin.contentassist.ContentAssistTestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class UroboroSQLContentAssistProcessorTest {

	@Test
	public void testCompareCharCustom() {
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('a', 'a'), is(0));
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('a', 'b'), is(-1));
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('a', '#'), is(-1));
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('#', '#'), is(0));
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('#', 'a'), is(1));
		assertThat(UroboroSQLContentAssistProcessor.compareCharCustom('b', 'a'), is(1));
	}

	@Test
	public void testComputeCompletionProposals01() {

		List<String> result = computeCompletionResults("/*");

		assertThat(result, is(Arrays.asList(
				"/*BEGIN*/\n|/*END*/",
				"/*ELIF |*/",
				"/*ELSE*/|",
				"/*END*/|",
				"/*IF |*/\n/*END*/",
				"/* _SQL_ID_ */|")));

		result = computeCompletionResults("/*E");

		assertThat(result, is(
				Arrays.asList(
						"/*ELIF |*/",
						"/*ELSE*/|",
						"/*END*/|")));

		result = computeCompletionResults("\t\t/*I");

		assertThat(result, is(
				Arrays.asList(
						"\t\t/*IF |*/\n\t\t/*END*/")));

		result = computeCompletionResults("    /*I");

		assertThat(result, is(
				Arrays.asList(
						"    /*IF |*/\n    /*END*/")));

		result = computeCompletionResults("    /*BEGIN");

		assertThat(result, is(
				Arrays.asList(
						"    /*BEGIN*/\n    |/*END*/")));

		result = computeCompletionResults("\n\r    /*BEGIN");

		assertThat(result, is(
				Arrays.asList(
						"\n\r    /*BEGIN*/\n    |/*END*/")));
	}

	@Test
	public void testComputeCompletionProposals02() {

		List<String> result = computeCompletionResults("TEST_VALUE /*");

		assertThat(result, is(
				Arrays.asList(
						"TEST_VALUE /*testValue*/''|",
						"TEST_VALUE /*BEGIN*/\n|/*END*/",
						"TEST_VALUE /*ELIF |*/",
						"TEST_VALUE /*ELSE*/|",
						"TEST_VALUE /*END*/|",
						"TEST_VALUE /*IF |*/\n/*END*/",
						"TEST_VALUE /* _SQL_ID_ */|")));

		result = computeCompletionResults("TEST_VALUE /*t");

		assertThat(result, is(
				Arrays.asList(
						"TEST_VALUE /*testValue*/''|")));

		result = computeCompletionResults("SELECT T.TEST_VALUE FROM DUMMY_TBL T /*t");

		assertThat(result, is(
				Arrays.asList(
						"SELECT T.TEST_VALUE FROM DUMMY_TBL T /*testValue*/''|")));
	}

	@Test
	public void testComputeCompletionProposals03() {

		List<String> result = computeCompletionResults("/*test_value*/ /*");

		assertThat(result, is(
				Arrays.asList(
						"/*test_value*/ /*test_value*/''|",
						"/*test_value*/ /*BEGIN*/\n|/*END*/",
						"/*test_value*/ /*ELIF |*/",
						"/*test_value*/ /*ELSE*/|",
						"/*test_value*/ /*END*/|",
						"/*test_value*/ /*IF |*/\n/*END*/",
						"/*test_value*/ /* _SQL_ID_ */|")));

		result = computeCompletionResults("/*test_value*/ /*t");

		assertThat(result, is(
				Arrays.asList(
						"/*test_value*/ /*test_value*/''|")));

		result = computeCompletionResults("/*IF data.a */ /*d");

		assertThat(result, is(
				Arrays.asList(
						"/*IF data.a */ /*data.a*/''|")));
	}

	@Test
	public void testComputeCompletionProposals04() {
		List<String> result = computeCompletionResults("/*IF ");

		assertThat(result, is(Arrays.asList(
				"/*IF SF|")));

		result = computeCompletionResults("/*IF SF.");

		assertThat(result, is(
				Arrays.asList(
						"/*IF SF.capitalize(|)",
						"/*IF SF.isBlank(|)",
						"/*IF SF.isEmpty(|)",
						"/*IF SF.isNotBlank(|)",
						"/*IF SF.isNotEmpty(|)",
						"/*IF SF.leftPad(|)",
						"/*IF SF.left(|)",
						"/*IF SF.mid(|)",
						"/*IF SF.rightPad(|)",
						"/*IF SF.right(|)",
						"/*IF SF.split(|)",
						"/*IF SF.trimToEmpty(|)",
						"/*IF SF.trim(|)",
						"/*IF SF.uncapitalize(|)")));

		result = computeCompletionResults("/*IF data.a */ /*IF d");

		assertThat(result, is(Arrays.asList(
				"/*IF data.a */ /*IF data|")));

		result = computeCompletionResults("/*IF data.a */ /*ELIF data.");

		assertThat(result, is(Arrays.asList(
				"/*IF data.a */ /*ELIF data.a|")));

		result = computeCompletionResults("/*IF data.a */ /*ELIF data.b");

		assertThat(result, is(Collections.emptyList()));

		result = computeCompletionResults("/*IF data.a */ /*ELIF data.a");

		assertThat(result, is(Collections.emptyList()));

		result = computeCompletionResults("/*IF data.a */ /*ELIF data.b.a");

		assertThat(result, is(Collections.emptyList()));

		result = computeCompletionResults("/*IF data.ab && data.ac */ /*ELIF data.a");

		assertThat(result, is(Arrays.asList(
				"/*IF data.ab && data.ac */ /*ELIF data.ab|",
				"/*IF data.ab && data.ac */ /*ELIF data.ac|")));
	}

	@Test
	public void testComputeCompletionProposals05() {
		List<String> result = computeCompletionResults("/*IF data */\n/*END*/ /*data*/123 /*d");

		assertThat(result, is(Arrays.asList(
				"/*IF data */\n/*END*/ /*data*/123 /*data*/123|")));

		result = computeCompletionResults("/*data*/123 /*IF data */\n/*END*/ /*d");

		assertThat(result, is(Arrays.asList(
				"/*data*/123 /*IF data */\n/*END*/ /*data*/123|")));
	}

	@Test
	public void testComputeCompletionProposals06() {
		List<String> result = computeCompletionResults(" /*data*/123 /*data", -3);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|")));

		result = computeCompletionResults(" /*data*/123 /*data22", -5);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|22")));

		result = computeCompletionResults(" /*data*/123 /*data*/123", -5);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|")));
	}

	@Test
	public void testComputeCompletionProposals07() {
		List<String> result = computeCompletionResults(" /*data*/123 /*data\n", -1);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|\n")));

		result = computeCompletionResults(" /*data*/123 /*data\na", -1);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|a")));

		result = computeCompletionResults(" /*data*/123 /*data*/\na", -4);

		assertThat(result, is(Arrays.asList(
				" /*data*/123 /*data*/123|\na")));
	}
}
