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
package com.github.s4ke.moar.regex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.s4ke.moar.strings.CodePointSet;
import com.github.s4ke.moar.util.RangeRep;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Utilities for Character Classes
 *
 * @author Martin Braun
 */
public class CharacterClassesUtils {

	private static final Set<Integer> WHITE_SPACE_CHARS = new HashSet<>(
			Arrays.asList(
					new Integer[] {
							(int) ' ',
							(int) '\t',
							(int) '\n',
							0x0B,
							(int) '\f',
							(int) '\r'
					}
			)
	);

	private static final Set<Integer> DIGITS =
			Arrays.asList( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 )
					.stream()
					.map( String::valueOf )
					.map( (str) -> str.codePointAt( 0 ) ).collect( Collectors.toSet() );

	private CharacterClassesUtils() {
		//can't touch this!
	}

	public static final String ANY = ".";
	public static final String WHITE_SPACE = "\\s";
	public static final String NON_WHITE_SPACE = "\\S";
	public static final String DIGIT = "\\d";
	public static final String NON_DIGIT = "\\D";
	public static final String WORD_CHARACTER = "\\w";
	public static final String NON_WORD_CHARACTER = "\\W";

	public static CodePointSet getFn(String identifier) {
		switch ( identifier ) {
			case ANY:
				return ANY_FN;
			case WHITE_SPACE:
				return WHITE_SPACE_FN;
			case NON_WHITE_SPACE:
				return NON_WHITE_SPACE_FN;
			case DIGIT:
				return DIGIT_FN;
			case NON_DIGIT:
				return NON_DIGIT_FN;
			case WORD_CHARACTER:
				return WORD_CHARACTER_FN;
			case NON_WORD_CHARACTER:
				return NON_WORD_CHARACTER_FN;
			default:
				throw new IllegalArgumentException( "unrecognized character class identifier: " + identifier );
		}
	}

	public static final CodePointSet ANY_FN = CodePointSet.range( RangeRep.of( Integer.MIN_VALUE, Integer.MAX_VALUE ) );

	public static final CodePointSet WHITE_SPACE_FN = CodePointSet.set( WHITE_SPACE_CHARS );

	public static final CodePointSet NON_WHITE_SPACE_FN = WHITE_SPACE_FN.negative();

	public static final CodePointSet DIGIT_FN = CodePointSet.set( DIGITS );

	public static final CodePointSet NON_DIGIT_FN = DIGIT_FN.negative();

	public static final CodePointSet WORD_CHARACTER_FN = CodePointSet.range(
			fromTo(
					'a',
					'z'
			), fromTo( 'A', 'Z' ), fromTo( '0', '9' ), fromTo( '_', '_' )
	);

	public static final CodePointSet NON_WORD_CHARACTER_FN = WORD_CHARACTER_FN.negative();

	static RangeRep fromTo(int from, int to) {
		return RangeRep.of( from, to );
	}

	public static CodePointSet positiveFn(Set<RangeRep> ranges) {
		RangeSet<Integer> rangeSet = TreeRangeSet.create();
		for ( RangeRep rangeRep : ranges ) {
			rangeSet.addAll( rangeRep.getRangeSet() );
		}
		return CodePointSet.range( RangeRep.of( rangeSet ) );
	}
}
