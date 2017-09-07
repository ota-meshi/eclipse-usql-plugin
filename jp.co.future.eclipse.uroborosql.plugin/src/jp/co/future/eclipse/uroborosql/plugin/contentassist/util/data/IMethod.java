package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.AbstractNamedNode;
import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.Method;

public interface IMethod extends INamedNode {

	static IMethod ofDef(String name, IDataType result, String... args) {
		return new Method(NodeLevel.DEF, name, result, args);
	}

	static Method ofDef(String name, Class<?> result, String... args) {
		return new Method(NodeLevel.DEF, name, IDataType.of(result), args);
	}

	static IMethod of(java.lang.reflect.Method method) {
		return new Method(NodeLevel.MEMBER, method.getName(), method);

	}

	static AbstractNamedNode<?> ofUnknown(String name, String... args) {
		return new Method(NodeLevel.UNKNOWN, name, IDataType.ofUnknown(), args);
	}
}
