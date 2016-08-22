/*
 The MIT License (MIT)

 Copyright (c) 2016 Martin Braun

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public class IntCharSeq implements CharSeq {
	private final int[] codePoints;

	public IntCharSeq(CharSequence seq) {
		this.codePoints = seq.codePoints().toArray();
	}

	@Override
	public int codePointLength() {
		return this.codePoints.length;
	}

	/**
	 * @param index the nth codepoint
	 */
	@Override
	public int codePoint(int index) {
		return this.codePoints[index];
	}

	@Override
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
