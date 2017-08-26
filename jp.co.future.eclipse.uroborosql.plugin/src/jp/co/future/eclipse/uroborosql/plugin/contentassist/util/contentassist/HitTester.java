package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class HitTester {

	public static OptionalInt hit(String input, String text) {
		return hit(input.codePoints().iterator(), text.codePoints().iterator());
	}

	public static OptionalInt hitTokens(String input, String text) {
		return hit(toTokenIterator(input.codePoints().iterator()), toTokenIterator(text.codePoints().iterator()));
	}

	private static OptionalInt hit(PrimitiveIterator.OfInt input, PrimitiveIterator.OfInt text) {
		int diffPoint = 0;
		boolean capitalTarget = true;
		while (input.hasNext()) {
			if (!text.hasNext()) {
				return OptionalInt.empty();
			}
			int oic = input.nextInt();
			int otc = text.nextInt();
			int ic;
			int tc;
			if (!capitalTarget) {
				//先頭文字以外はignore caseで比較する
				ic = Character.toUpperCase(oic);
				tc = Character.toUpperCase(otc);
			} else {
				ic = oic;
				tc = otc;
			}
			if (ic != tc) {
				return OptionalInt.empty();
			}
			diffPoint += oic == otc ? 0 : 1;
			//次が先頭文字かどうか
			capitalTarget = !isStandard(tc);
		}

		return OptionalInt.of(diffPoint);
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

	private static boolean isStandard(int c) {
		return Character.isAlphabetic(c) || Character.isDigit(c);
	}

}
