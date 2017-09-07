package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql;

import static jp.co.future.eclipse.uroborosql.plugin.contentassist.ContentAssistTestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UroboroSQLUtilsTest {

	@Test
	public void testSF() {

		List<String> result = computeCompletionResults("/*IF SF.capitalize(s).i");

		assertThat(result, is(
				Arrays.asList(
						"/*IF SF.capitalize(s).indexOf(|_arg0|)",
						"/*IF SF.capitalize(s).indexOf(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).intern(|)",
						"/*IF SF.capitalize(s).isEmpty(|)"

				)));

		result = computeCompletionResults("/*IF SF.capitalize(s).");

		assertThat(result, is(
				Arrays.asList(
						"/*IF SF.capitalize(s).chars(|)",
						"/*IF SF.capitalize(s).charAt(|_arg0|)",
						"/*IF SF.capitalize(s).codePoints(|)",
						"/*IF SF.capitalize(s).codePointAt(|_arg0|)",
						"/*IF SF.capitalize(s).codePointBefore(|_arg0|)",
						"/*IF SF.capitalize(s).codePointCount(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).compareToIgnoreCase(|_arg0|)",
						"/*IF SF.capitalize(s).compareTo(|_arg0|)",
						"/*IF SF.capitalize(s).concat(|_arg0|)",
						"/*IF SF.capitalize(s).contains(|_arg0|)",
						"/*IF SF.capitalize(s).contentEquals(|_arg0|)",
						"/*IF SF.capitalize(s).copyValueOf(|_arg0|)",
						"/*IF SF.capitalize(s).copyValueOf(|_arg0, _arg1, _arg2|)",
						"/*IF SF.capitalize(s).endsWith(|_arg0|)",
						"/*IF SF.capitalize(s).equalsIgnoreCase(|_arg0|)",
						"/*IF SF.capitalize(s).equals(|_arg0|)",
						"/*IF SF.capitalize(s).format(|_arg0, _arg1, _arg2|)",
						"/*IF SF.capitalize(s).format(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).getBytes(|_arg0, _arg1, _arg2, _arg3|)",
						"/*IF SF.capitalize(s).getBytes(|_arg0|)",
						"/*IF SF.capitalize(s).getBytes(|)",
						"/*IF SF.capitalize(s).getChars(|_arg0, _arg1, _arg2, _arg3|)",
						"/*IF SF.capitalize(s).getClass(|)",
						"/*IF SF.capitalize(s).hashCode(|)",
						"/*IF SF.capitalize(s).indexOf(|_arg0|)",
						"/*IF SF.capitalize(s).indexOf(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).intern(|)",
						"/*IF SF.capitalize(s).isEmpty(|)",
						"/*IF SF.capitalize(s).join(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).lastIndexOf(|_arg0|)",
						"/*IF SF.capitalize(s).lastIndexOf(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).length(|)",
						"/*IF SF.capitalize(s).matches(|_arg0|)",
						"/*IF SF.capitalize(s).notifyAll(|)",
						"/*IF SF.capitalize(s).notify(|)",
						"/*IF SF.capitalize(s).offsetByCodePoints(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).regionMatches(|_arg0, _arg1, _arg2, _arg3, _arg4|)",
						"/*IF SF.capitalize(s).regionMatches(|_arg0, _arg1, _arg2, _arg3|)",
						"/*IF SF.capitalize(s).replaceAll(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).replaceFirst(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).replace(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).split(|_arg0|)",
						"/*IF SF.capitalize(s).split(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).startsWith(|_arg0|)",
						"/*IF SF.capitalize(s).startsWith(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).substring(|_arg0|)",
						"/*IF SF.capitalize(s).substring(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).subSequence(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).toCharArray(|)",
						"/*IF SF.capitalize(s).toLowerCase(|_arg0|)",
						"/*IF SF.capitalize(s).toLowerCase(|)",
						"/*IF SF.capitalize(s).toString(|)",
						"/*IF SF.capitalize(s).toUpperCase(|_arg0|)",
						"/*IF SF.capitalize(s).toUpperCase(|)",
						"/*IF SF.capitalize(s).trim(|)",
						"/*IF SF.capitalize(s).valueOf(|_arg0, _arg1, _arg2|)",
						"/*IF SF.capitalize(s).valueOf(|_arg0|)",
						"/*IF SF.capitalize(s).wait(|_arg0|)",
						"/*IF SF.capitalize(s).wait(|_arg0, _arg1|)",
						"/*IF SF.capitalize(s).wait(|)",
						"/*IF SF.capitalize(s).CASE_INSENSITIVE_ORDER|"

				)));
	}

}
