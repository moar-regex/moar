package com.github.s4ke.moar.regex;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public final class BoundConstants {

	private BoundConstants() {
		//can't touch this!
	}

	public static final int START_OF_LINE = 1;
	public static final int END_OF_LINE = 2;
	public static final int START_OF_INPUT = 3;
	public static final int END_OF_INPUT = 4;
	public static final int END_OF_LAST_MATCH = 5;

	public static final Set<EfficientString> LINE_BREAK_CHARS = Arrays.asList(
			"\n",
			"\r\n",
			"\u2029",
			//Java pattern logic...
			String.valueOf( (char) ('\u2029' - 1) ),
			"\u0085"
	).stream().map( EfficientString::new ).collect(
			Collectors.toSet()
	);

}
