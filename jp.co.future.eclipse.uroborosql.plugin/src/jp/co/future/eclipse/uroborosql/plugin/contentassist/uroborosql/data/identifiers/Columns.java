package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.identifiers;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import jp.co.future.eclipse.uroborosql.plugin.utils.Strings;

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
					list.set(i, e);
					return true;
				}
				if (Strings.isNotEmpty(o.getComment()) && Strings.isNotEmpty(o.getDescription())) {
					return false;
				}

				list.set(i, new Column(e, o));
				return true;
			}
		}
		return list.add(e);
	}
}
