package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.List;

public class Replacement {
	final String[] replacementStrings;
	final int cursorPosition;

	public Replacement(String replacementString) {
		replacementStrings = new String[] { replacementString };
		cursorPosition = replacementString.length();
	}

	public Replacement(String replacementString, int cursorPosition) {
		replacementStrings = new String[] { replacementString };
		this.cursorPosition = cursorPosition;
	}

	public Replacement(String[] replacementStrings, int cursorPosition) {
		this.replacementStrings = replacementStrings;
		this.cursorPosition = cursorPosition;
	}

	public Replacement(List<String> replacementStrings, int cursorPosition) {
		this.replacementStrings = replacementStrings.toArray(new String[replacementStrings.size()]);
		this.cursorPosition = cursorPosition;
	}
}