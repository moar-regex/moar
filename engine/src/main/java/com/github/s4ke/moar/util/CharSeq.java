package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public class CharSeq {
	private final int[] codePoints;

	public CharSeq(CharSequence seq) {
		this.codePoints = seq.codePoints().toArray();
	}

	public int codePointLength() {
		return this.codePoints.length;
	}

	/**
	 * @param index the nth codepoint
	 */
	public int codePoint(int index) {
		return this.codePoints[index];
	}

	public String subSequence(int start, int end) {
		return new String( this.codePoints, start, end );
	}

	@Override
	public String toString() {
		return new String( this.codePoints, 0, this.codePoints.length );
	}

}
