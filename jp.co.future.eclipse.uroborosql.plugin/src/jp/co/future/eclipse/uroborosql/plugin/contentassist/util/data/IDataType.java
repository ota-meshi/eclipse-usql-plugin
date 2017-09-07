package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data;

import java.util.Arrays;

import jp.co.future.eclipse.uroborosql.plugin.contentassist.util.data.impl.Branch;

public interface IDataType {

	static IDataType of(Class<?> type) {
		return new Branch(NodeLevel.MEMBER, "", type);
	}

	static IDataType[] of(Class<?>[] args) {
		return Arrays.stream(args).map(IDataType::of).toArray(IDataType[]::new);
	}

	static IDataType ofUnknown() {
		return IBranch.ofUnknown("");
	}

}
