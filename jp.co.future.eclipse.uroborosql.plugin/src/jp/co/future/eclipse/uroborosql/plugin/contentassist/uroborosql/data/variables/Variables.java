package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Variables implements Iterable<IVariable> {
	private final Map<String, IVariable> map = new HashMap<>();

	public boolean containsVariable(String variableName) {
		return map.containsKey(variableName);
	}

	public Variables put(IVariable variable) {
		String key = variable.getVariableName();
		IVariable old = map.get(key);
		if (old == null) {
			map.put(key, variable);
		} else if (old.getSqlValue() == null || old.getSqlValue().equals("''") || old.getDescription() == null) {
			map.put(key, variable.marge(old));
		}
		return this;
	}

	public Variables putAll(Variables variables) {
		for (IVariable variable : variables) {
			put(variable);
		}
		return this;
	}

	public Collection<IVariable> variables() {
		return map.values();
	}

	@Override
	public Iterator<IVariable> iterator() {
		return variables().iterator();
	}

	public static Collector<IVariable, ?, Variables> toVariables() {
		return Collector.of(Variables::new, (vs, v) -> vs.put(v), Variables::putAll);
	}

	public static Collector<Variables, ?, Optional<Variables>> reducing() {
		return Collectors.reducing(Variables::putAll);
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public Map<String, IVariable> asMap() {
		return Collections.unmodifiableMap(map);
	}

}