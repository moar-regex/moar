package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public interface CharSeq {
	int codePointLength();

	int codePoint(int index);

	String subSequence(int start, int end);

	@Override
	String toString();
}
