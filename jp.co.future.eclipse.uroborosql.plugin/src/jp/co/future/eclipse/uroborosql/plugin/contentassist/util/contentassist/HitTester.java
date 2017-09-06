package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.Collection;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.IntStream;

import jp.co.future.eclipse.uroborosql.plugin.utils.Characters;

public class HitTester {
	private static final int OFFSET_ORDERED = 20;
	private static final int OFFSET_SEARCH = 30;

	private static final int ORDERED_ACCEPT_INTERVAL = 3;

	public static OptionalInt hit(String input, String text) {
		return hit(input.codePoints().iterator(), text.codePoints().iterator());
	}

	public static OptionalInt hitTokens(String input, String text) {
		return hit(toTokenIterator(input.codePoints().iterator()), toTokenIterator(text.codePoints().iterator()));
	}

	public static OptionalInt search(String input, Collection<String> targetTexts) {
		Set<Integer> points = new HashSet<>();
		for (String text : targetTexts) {
			hitTokens(input, text).ifPresent(p -> points.add(p));
		}
		if (!points.isEmpty()) {
			return points.stream().mapToInt(i -> i).min();
		}
		for (String text : targetTexts) {
			orderedHit(toTokenIterator(input.codePoints().iterator()),
					toTokenIterator(text.codePoints().iterator())).ifPresent(p -> points.add(p + OFFSET_ORDERED));
		}
		if (!points.isEmpty()) {
			return points.stream().mapToInt(i -> i).min();
		}
		for (String text : targetTexts) {
			search(tokens(input), tokens(text)).ifPresent(p -> points.add(p + OFFSET_SEARCH));
		}

		return points.stream().mapToInt(i -> i).min();
	}

	private static OptionalInt hit(PrimitiveIterator.OfInt input, PrimitiveIterator.OfInt text) {
		int diffPoint = 0;
		boolean capitalTarget = true;
		while (input.hasNext()) {
			if (!text.hasNext()) {
				return OptionalInt.empty();
			}
			int ic = input.nextInt();
			int tc = text.nextInt();
			if (!capitalTarget) {
				//先頭文字以外はignore caseで比較する
				if (!Characters.equalsIgnoreCase(ic, tc)) {
					return OptionalInt.empty();
				}
			} else {
				if (ic != tc) {
					return OptionalInt.empty();
				}
			}

			diffPoint += ic == tc ? 0 : 1;
			//次が先頭文字かどうか
			capitalTarget = !isStandard(tc);
		}

		return OptionalInt.of(diffPoint);
	}

	private static OptionalInt orderedHit(PrimitiveIterator.OfInt input, PrimitiveIterator.OfInt text) {
		boolean first = true;
		int intarval = 0;

		int diffPoint = 0;
		inputLoop: while (input.hasNext()) {
			int ic = input.nextInt();
			while (text.hasNext()) {
				int tc = text.nextInt();
				if (Characters.equalsIgnoreCase(ic, tc)) {
					first = false;
					intarval = 0;
					continue inputLoop;
				}
				intarval++;
				if (!first && ORDERED_ACCEPT_INTERVAL < intarval) {
					return OptionalInt.empty();
				}

				diffPoint++;
			}
			return OptionalInt.empty();
		}
		return OptionalInt.of(diffPoint);
	}

	private static OptionalInt search(Collection<String> input, Collection<String> text) {
		inputLoop: for (String in : input) {
			for (String tx : text) {
				if (tx.toUpperCase().contains(in.toUpperCase())) {
					continue inputLoop;
				}
			}
			return OptionalInt.empty();
		}
		return OptionalInt.of(0);
	}

	private static class TokenIterator implements PrimitiveIterator.OfInt {
		private final PrimitiveIterator.OfInt base;
		private PrimitiveIterator.OfInt nextIterator = IntStream.empty().iterator();
		int prev = ' ';

		TokenIterator(OfInt base) {
			this.base = base;
		}

		@Override
		public boolean hasNext() {
			return getNextIterator().hasNext();
		}

		@Override
		public int nextInt() {
			return getNextIterator().nextInt();
		}

		private PrimitiveIterator.OfInt getNextIterator() {
			if (nextIterator.hasNext()) {
				return nextIterator;
			}
			if (!base.hasNext()) {
				return base;
			}
			boolean hasWs = false;
			int next = base.nextInt();
			while (Character.isWhitespace(next)) {
				if (!base.hasNext()) {
					return base;
				}
				next = base.nextInt();
				hasWs = true;
			}
			if (hasWs && isStandard(next) && isStandard(prev)) {
				prev = next;
				return nextIterator = IntStream.of(' ', next).iterator();
			} else {
				prev = next;
				return nextIterator = IntStream.of(next).iterator();
			}
		}
	}

	static PrimitiveIterator.OfInt toTokenIterator(PrimitiveIterator.OfInt iterator) {
		if (!iterator.hasNext()) {
			return iterator;
		}
		return new TokenIterator(iterator);
	}

	private static Set<String> tokens(String s) {
		Set<String> result = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		boolean prestd = false;

		for (PrimitiveIterator.OfInt iterator = s.codePoints().iterator(); iterator.hasNext();) {
			int c = iterator.nextInt();
			if (Character.isWhitespace(c) || isSymbol(c)) {
				if (sb.length() > 0) {
					result.add(sb.toString());
				}
				sb.setLength(0);
				continue;
			}
			boolean std = isStandard(c);
			if (std != prestd) {
				if (sb.length() > 0) {
					result.add(sb.toString());
				}
				sb.setLength(0);
				prestd = std;
			}
			sb.append(Character.toChars(c));
		}
		if (sb.length() > 0) {
			result.add(sb.toString());
		}
		return result;
	}

	private static boolean isStandard(int c) {
		return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
	}

	private static boolean isSymbol(final int c) {
		switch (c) {
		case '"': // double quote
		case '?': // question mark
		case '%': // percent
		case '&': // ampersand
		case '\'': // quote
		case '(': // left paren
		case ')': // right paren
		case '|': // vertical bar
		case '*': // asterisk
		case '+': // plus sign
		case ',': // comma
		case '-': // minus sign
		case '.': // period
		case '/': // solidus
		case ':': // colon
		case ';': // semicolon
		case '<': // less than operator
		case '=': // equals operator
		case '>': // greater than operator
		case '#':
		case '_': //underscore
		case '!':
		case '$':
		case '[':
		case '\\':
		case ']':
		case '^':
		case '{':
		case '}':
		case '~':
			return true;
		default:
			return false;
		}
	}

}
