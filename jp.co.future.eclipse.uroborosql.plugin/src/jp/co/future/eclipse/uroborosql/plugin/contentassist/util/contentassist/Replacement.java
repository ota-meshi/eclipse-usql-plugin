package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class Replacement {
	final String[] replacementStrings;
	final OptionalInt cursorPosition;
	final boolean needLinefeed;

	public Replacement(String replacementString, boolean needLinefeed) {
		replacementStrings = new String[] { replacementString };
		cursorPosition = OptionalInt.empty();
		this.needLinefeed = needLinefeed;
	}

	public Replacement(String replacementString, int cursorPosition, boolean needLinefeed) {
		replacementStrings = new String[] { replacementString };
		this.cursorPosition = OptionalInt.of(cursorPosition);
		this.needLinefeed = needLinefeed;
	}

	public Replacement(String[] replacementStrings, int cursorPosition, boolean needLinefeed) {
		this.replacementStrings = replacementStrings;
		this.cursorPosition = OptionalInt.of(cursorPosition);
		this.needLinefeed = needLinefeed;
	}

	public Replacement(List<String> replacementStrings, int cursorPosition, boolean needLinefeed) {
		this.replacementStrings = replacementStrings.toArray(new String[replacementStrings.size()]);
		this.cursorPosition = OptionalInt.of(cursorPosition);
		this.needLinefeed = needLinefeed;
	}

	public Replacement(List<String> replacementStrings, boolean needLinefeed) {
		this.replacementStrings = replacementStrings.toArray(new String[replacementStrings.size()]);
		cursorPosition = OptionalInt.empty();
		this.needLinefeed = needLinefeed;
	}

	public boolean isNeedLinefeed() {
		return needLinefeed;
	}

	public List<String> getReplacementStrings(String prefix, String indent) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < replacementStrings.length; i++) {
			String replacementString = replacementStrings[i];
			if (i == 0) {
				result.add(prefix + indent + replacementString);
			} else {
				result.add(indent + replacementString);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		if (replacementStrings.length > 1) {
			return replacementStrings[0] + "...";
		} else {
			return replacementStrings[0];
		}
	}
}