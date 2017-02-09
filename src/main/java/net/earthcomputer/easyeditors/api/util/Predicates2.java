package net.earthcomputer.easyeditors.api.util;

import java.util.regex.Pattern;

import com.google.common.base.Predicate;

/**
 * A class which provides more functions to do with predicates.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class Predicates2 {

	private Predicates2() {
	}

	/**
	 * Creates a copy of the given predicate, such that for any predicate x, the
	 * following statements are true:
	 * <ul>
	 * <li><code>x != Predicates2.copyOf(x)</code></li>
	 * <li><code>!x.equals(Predicates2.copyOf(x))</code></li>
	 * <li><code>x.apply(y) == Predicates2.copyOf(x).apply(y)</code></li>
	 * </ul>
	 * 
	 * @param predicate
	 * @return
	 */
	public static <T> Predicate<T> copyOf(Predicate<T> predicate) {
		return new CopyPredicate<T>(predicate);
	}

	/**
	 * Creates a predicate which will return true to inputs where
	 * <code>pattern.matcher(input).matches()</code>.
	 * 
	 * @param pattern
	 * @return
	 */
	public static <T extends CharSequence> Predicate<T> matchingPattern(Pattern pattern) {
		return new PatternMatchingPredicate<T>(pattern);
	}

	private static class CopyPredicate<T> implements Predicate<T> {
		private Predicate<T> other;

		public CopyPredicate(Predicate<T> other) {
			this.other = other;
		}

		@Override
		public boolean apply(T input) {
			return other.apply(input);
		}
	}

	private static class PatternMatchingPredicate<T extends CharSequence> implements Predicate<T> {
		private Pattern pattern;

		public PatternMatchingPredicate(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean apply(T input) {
			return pattern.matcher(input).matches();
		}
	}

}
