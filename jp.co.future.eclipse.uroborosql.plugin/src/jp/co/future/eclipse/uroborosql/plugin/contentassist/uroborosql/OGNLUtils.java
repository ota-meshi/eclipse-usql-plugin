package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import ognl.ASTChain;
import ognl.ASTMethod;
import ognl.ASTProperty;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.SimpleNode;

public class OGNLUtils {

	public static Set<String> getVariableNames(String expression) {
		Node parsedExpression;
		try {
			parsedExpression = (Node) Ognl.parseExpression(expression);
		} catch (OgnlException e) {
			return Collections.emptySet();
		}
		if (parsedExpression instanceof SimpleNode) {
			SimpleNode simpleNode = (SimpleNode) parsedExpression;
			return getVariableNames(simpleNode);
		}
		return Collections.emptySet();
	}

	private static Set<String> getVariableNames(SimpleNode node) {

		if (node instanceof ASTProperty) {
			ASTProperty astProperty = (ASTProperty) node;
			return new HashSet<>(Arrays.asList(astProperty.toString()));
		} else if (node instanceof ASTMethod) {
			ASTMethod astMethod = (ASTMethod) node;
			return new HashSet<>(Arrays.asList(astMethod.getMethodName() + "()"));
		} else if (node instanceof ASTChain) {//nest
			ASTChain astChain = (ASTChain) node;
			return new HashSet<>(Arrays.asList(getChainVariableName(astChain)));
		} else {
			Set<String> result = new HashSet<>();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node child = node.jjtGetChild(i);

				if (child instanceof SimpleNode) {
					result.addAll(getVariableNames(SimpleNode.class.cast(child)));
				}
			}
			return result;
		}
	}

	private static String getChainVariableName(ASTChain node) {
		StringJoiner joiner = new StringJoiner(".");

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node child = node.jjtGetChild(i);

			if (child instanceof ASTProperty) {
				ASTProperty astProperty = (ASTProperty) child;
				if (astProperty.isIndexedAccess()) {
					break;
				}
				joiner.add(astProperty.toString());
			} else if (child instanceof ASTMethod) {
				ASTMethod astMethod = (ASTMethod) child;
				joiner.add(astMethod.getMethodName() + "()");
				break;
			} else {
				break;
			}
		}

		return joiner.toString();
	}
}
