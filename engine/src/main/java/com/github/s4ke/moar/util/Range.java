package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public class Range {

	public final int from;
	public final int to;

	private Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public static Range of(int from, int to) {
		return new Range( from, to );
	}

}
