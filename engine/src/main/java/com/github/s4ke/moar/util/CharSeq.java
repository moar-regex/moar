package com.github.s4ke.moar.util;

import java.util.stream.IntStream;

/**
 * @author Martin Braun
 */
public class CharSeq {
	private final CharSequence seq;
	private int len = -1;

	public CharSeq(CharSequence seq) {
		this.seq = seq;
	}

	public int codePointLength() {
		if ( this.len == -1 ) {
			this.len = Character.codePointCount( this.seq, 0, this.seq.length() );
		}
		return this.len;
	}

	public int length() {
		throw new AssertionError( "hey don't use deprecated APIs!" );
	}

	public char charAt(int index) {
		throw new AssertionError( "hey don't use deprecated APIs!" );
	}

	public int codePointAt(int index) {
		return Character.codePointAt( this.seq, index );
	}

	public CharSequence subSequence(int start, int end) {
		return seq.subSequence( start, end );
	}

	@Override
	public String toString() {
		return seq.toString();
	}

	public IntStream chars() {
		return seq.chars();
	}

	public IntStream codePoints() {
		return seq.codePoints();
	}
}
