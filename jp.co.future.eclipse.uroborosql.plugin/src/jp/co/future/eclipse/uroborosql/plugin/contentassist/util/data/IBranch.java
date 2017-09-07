package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data;

import java.lang.reflect.Field;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.Branch;

public interface IBranch extends INamedNode, IDataType {

	IBranch getOrCreateBranch(String name);

	boolean isEmpty();

	static Branch ofDef(String name, INamedNode... children) {
		Branch branch = new Branch(NodeLevel.DEF, name);
		for (INamedNode node : children) {
			branch.putChild(node);
		}
		return branch;
	}

	static Branch ofValue(String name, Object value) {
		if (value == null) {
			return new Branch(NodeLevel.UNKNOWN, name, void.class);
		}

		if (value.getClass().equals(Object.class)) {
			return new Branch(NodeLevel.UNKNOWN, name, Object.class);
		}

		return new Branch(NodeLevel.MEMBER, name, value.getClass());
	}

	static Branch ofUnknown(String name) {
		return new Branch(NodeLevel.UNKNOWN, name, void.class);
	}

	static Branch of(Field field) {
		return new Branch(NodeLevel.UNKNOWN, field.getName(), field.getType());
	}

}
