package jp.co.future.eclipse.uroborosql.plugin.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.PrimitiveIterator;

public class Strings {
	public static String toCamel(final String original) {
		if (original == null || original.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		PrimitiveIterator.OfInt ci = original.trim().toLowerCase().codePoints().iterator();
		while (ci.hasNext()) {
			int codePoint = ci.nextInt();
			if (codePoint == '_') {
				if (ci.hasNext()) {
					codePoint = ci.nextInt();
					builder.append(Character.toChars(Character.toUpperCase(codePoint)));
				}
			} else {
				builder.append(Character.toChars(codePoint));
			}
		}
		return builder.toString();
	}

	public static String rightTabs(String name, int maxWidths) {
		int maxTabWidths = (maxWidths & 3) == 0 ? maxWidths : maxWidths - maxWidths % 4 + 4;

		int widths = widths(name);
		int diff = maxTabWidths - widths;
		int tabCount = diff / 4 + ((diff & 3) == 0 ? 0 : 1);
		StringBuilder sb = new StringBuilder(name);
		for (int i = 0; i < tabCount; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	public static int widths(String s) {
		return s.codePoints().map(p -> widths(p)).sum();
	}

	private static int widths(int point) {
		if (point == '\t') {
			return 4;
		}

		CharBuffer cb = CharBuffer.wrap(Character.toChars(point));
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.reset();
		ByteBuffer bb = ByteBuffer.allocate((int) (encoder.maxBytesPerChar() * 2));
		encoder.encode(cb, bb, true);
		return bb.position() > 1 ? 2 : 1;
	}
}
