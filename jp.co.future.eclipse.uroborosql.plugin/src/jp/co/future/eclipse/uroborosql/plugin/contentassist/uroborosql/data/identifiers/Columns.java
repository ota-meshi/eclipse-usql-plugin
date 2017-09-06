package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;
import jp.co.future.eclipse.uroborosql.plugin.utils.collection.Lists;

public class Columns extends AbstractList<Column> {
	private final List<Column> list = new ArrayList<>();

	@Override
	public Column get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean add(Column e) {
		for (int i = 0; i < list.size(); i++) {
			Column o = list.get(i);
			if (o.getName().equalsIgnoreCase(e.getName())) {
				if (Strings.isEmpty(o.getComment()) && Strings.isEmpty(o.getDescription())) {
					return false;
				}
				String comment = Lists.asList(o.getComment(), e.getComment())
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null);
				String description = Lists.asList(o.getDescription(), e.getDescription())
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null);
				list.set(i, new Column(e.table, e.getName(), comment, description));
				return true;
			}
		}
		return list.add(e);
	}
}
