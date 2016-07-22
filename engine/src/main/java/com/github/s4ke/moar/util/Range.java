package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public class Range {

	public final char from;
	public final char to;

	private Range(char from, char to) {
		this.from = from;
		this.to = to;
	}

	public static Range of(char from, char to) {
		return new Range( from, to );
	}

}
