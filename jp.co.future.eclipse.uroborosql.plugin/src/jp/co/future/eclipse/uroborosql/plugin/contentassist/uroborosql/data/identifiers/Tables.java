package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class Tables extends AbstractSet<Table> {
	private final Map<String, Table> map = new HashMap<>();

	@Override
	public Iterator<Table> iterator() {
		return map.values().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean add(Table e) {

		return map.compute(e.getName().toLowerCase(), (k, o) -> {
			if (o == null) {
				return e;
			}
			if (isEmpty(o.getComment()) && isEmpty(o.getDescription())) {
				return e;
			}
			String comment = getDefined(e.getComment(), o.getComment());
			String description = getDefined(e.getDescription(), o.getDescription());
			return new Table(e.config, e.getName(), comment, description);
		}).equals(e);
	}

	private String getDefined(String... ss) {
		for (String s : ss) {
			if (!isEmpty(s)) {
				return s;
			}
		}
		return ss[0];
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public Optional<Table> get(String tableName) {
		return Optional.ofNullable(map.get(tableName.toLowerCase()));
	}
}
