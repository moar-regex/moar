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
		if ( end - start > 0 ) {
			int[] codePointArr = new int[end - start];
			System.arraycopy(this.codePoints, start, codePointArr, 0, end - start);
			return new String( codePointArr, 0, end - start );
		}
		else {
			return "";
		}
	}

	@Override
	public String toString() {
		return new String( this.codePoints, 0, this.codePoints.length );
	}

}
