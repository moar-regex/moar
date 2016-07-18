package com.github.s4ke.moar.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Martin Braun
 */
public class PatternMatcher implements GenericMatcher {

	private final Pattern pattern;

	public PatternMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean check(String str) {
		return this.pattern.matcher( str ).matches();
	}

	@Override
	public String toString() {
		return "PatternMatcher{" +
				"pattern=" + pattern +
				'}';
	}
}
