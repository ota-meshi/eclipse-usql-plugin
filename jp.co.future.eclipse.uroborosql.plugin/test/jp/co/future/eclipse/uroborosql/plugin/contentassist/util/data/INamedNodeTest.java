package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

public class INamedNodeTest {

	@Test
	public void testOfUnknownToken() {
		INamedNode node = INamedNode.ofUnknownToken("method(aaa,bbb,ccc)");
		assertThat(node, is(instanceOf(IMethod.class)));

		IMethod m = (IMethod) node;

	}

}
