package jp.co.future.eclipse.uroborosql.plugin.contentassist.util.contentassist;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.junit.Test;

public class HitTesterTest {

	@Test
	public void testHit() {
		assertThat(HitTester.hit("a", "a"), is(OptionalInt.of(0)));
		assertThat(HitTester.hit("a", "ab"), is(OptionalInt.of(0)));
		assertThat(HitTester.hit("a", "b"), is(OptionalInt.empty()));
		assertThat(HitTester.hit("ab", "a"), is(OptionalInt.empty()));

		assertThat(HitTester.hit("A", "a"), is(OptionalInt.empty()));
		assertThat(HitTester.hit("a", "A"), is(OptionalInt.empty()));

		assertThat(HitTester.hit("Abc", "ABCdefg"), is(OptionalInt.of(2)));
		assertThat(HitTester.hit("aBC", "abcdefg"), is(OptionalInt.of(2)));

		assertThat(HitTester.hit("Abc$Def", "ABC$DEFGHI"), is(OptionalInt.of(4)));
		assertThat(HitTester.hit("Abc$def", "ABC$DEFGHI"), is(OptionalInt.empty()));
	}

	@Test
	public void testHitTokens() {
		assertThat(HitTester.hitTokens("Abc $ Def", "ABC$DEFGHI"), is(OptionalInt.of(4)));
		assertThat(HitTester.hitTokens("Abc $ def", "ABC$DEFGHI"), is(OptionalInt.empty()));
	}

	@Test
	public void testToTokenIterator() {

		assertThat(toTokenIterator("Abc $    def   Ghi    .123 jklm "), is("Abc$def Ghi.123 jklm"));
		assertThat(toTokenIterator("  _   Abc"), is("_Abc"));
	}

	private String toTokenIterator(String string) {
		IntStream.Builder builder = IntStream.builder();
		HitTester.toTokenIterator(string.codePoints().iterator()).forEachRemaining((IntConsumer) builder::accept);
		int[] points = builder.build().toArray();
		return new String(points, 0, points.length);
	}

}
