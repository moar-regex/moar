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
package com.github.s4ke.moar.strings;

import com.github.s4ke.moar.util.CharSeq;
import com.github.s4ke.moar.util.IntCharSeq;

/**
 * <p>
 * A String representation implementation that allows us to
 * create SubSequences without
 * having to reallocate arrays.
 * <p/>
 * <p>
 * This helps to reduce the amount of time spent
 * building String objects and also reduces the amount
 * of memory used compared to basic Strings.
 * </p>
 *
 * @author Martin Braun
 */
public class EfficientString implements Comparable<EfficientString> {

	private CharSeq underlying;
	private int start;
	private int end;

	public EfficientString() {
		this.underlying = null;
		this.start = 0;
		this.end = 0;
	}

	public EfficientString(CharSeq underlying, int start, int end) {
		this.underlying = underlying;
		this.start = start;
		this.end = end;
	}

	public EfficientString(CharSeq underlying) {
		this.underlying = underlying;
		this.start = 0;
		this.end = underlying.codePointLength();
	}

	public EfficientString(CharSequence underlying) {
		this( new IntCharSeq( underlying ) );
	}

	public void update(EfficientString underlying, int start, int end) {
		this.underlying = underlying.underlying;
		this.start = start;
		this.end = end;
	}

	public void appendOrOverwrite(EfficientString substring) {
		if ( this.underlying == null ) {
			this.update( substring, substring.start, substring.end );
		}
		else {
			if ( this.end != substring.start || this.underlying != substring.underlying ) {
				this.update( substring, substring.start, substring.end );
			}
			this.end = substring.end;
		}
	}

	public void update(CharSeq underlying, int start, int end) {
		this.underlying = underlying;
		this.start = start;
		this.end = end;
	}

	public void reset() {
		this.underlying = null;
		this.start = 0;
		this.end = 0;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}

		//these checks are important so we can have our special char behaviour
		//for i.e. ^ and $
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		EfficientString efficientString = (EfficientString) o;

		return this.equalTo( efficientString );
	}

	public boolean equalTo(EfficientString str) {
		int ownLength = this.codePointLength();
		if ( ownLength != str.codePointLength() ) {
			return false;
		}
		for ( int i = 0; i < ownLength; ++i ) {
			if ( this.codePoint( i ) != str.codePoint( i ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for ( int i = 0; i < this.codePointLength(); ++i ) {
			result = 31 * result + Integer.hashCode( this.codePoint( i ) );
		}
		result = 31 * result + Integer.hashCode( this.codePointLength() );
		return result;
	}

	public int codePointLength() {
		return this.end - start;
	}

	/**
	 * @param index the nth codepoint
	 */
	public int codePoint(int index) {
		if ( this.underlying == null ) {
			throw new IndexOutOfBoundsException();
		}
		return this.underlying.codePoint( this.start + index );
	}

	public EfficientString subSequence(int start, int end) {
		if ( end - start > this.codePointLength() ) {
			throw new IndexOutOfBoundsException();
		}
		return new EfficientString( this.underlying, this.start + start, this.end + end );
	}

	@Override
	public String toString() {
		if ( this.underlying == null ) {
			return "";
		}
		if ( this.codePointLength() == this.underlying.codePointLength() ) {
			return this.underlying.toString();
		}
		return this.underlying.subSequence( this.start, this.end );
	}

	@Override
	public int compareTo(EfficientString o) {
		//similar to Java's String comparison fn

		int len1 = this.codePointLength();
		int len2 = o.codePointLength();
		int lim = Math.min( len1, len2 );

		int k = 0;
		while ( k < lim ) {
			int c1 = this.codePoint( k );
			int c2 = o.codePoint( k );
			if ( c1 != c2 ) {
				return c1 - c2;
			}
			k++;
		}
		return len1 - len2;
	}
}
