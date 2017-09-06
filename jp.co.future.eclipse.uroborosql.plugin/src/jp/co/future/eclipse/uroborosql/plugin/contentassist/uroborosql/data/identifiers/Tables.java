package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Lists;

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
			if (Strings.isEmpty(o.getComment()) && Strings.isEmpty(o.getDescription())) {
				return e;
			}
			String comment = Lists.asList(o.getComment(), e.getComment())
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
			String description = Lists.asList(o.getDescription(), e.getDescription())
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
			return new Table(e.config, e.getName(), comment, description);
		}).equals(e);
	}

	public Optional<Table> get(String tableName) {
		return Optional.ofNullable(map.get(tableName.toLowerCase()));
	}
}
