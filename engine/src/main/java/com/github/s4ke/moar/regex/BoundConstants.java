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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.s4ke.moar.moa.states.MatchInfo;
import com.github.s4ke.moar.strings.EfficientString;
import com.github.s4ke.moar.util.CharSeq;

/**
 * Constants for the supported boundary checks
 *
 * @author Martin Braun
 */
public final class BoundConstants {

	private BoundConstants() {
		//can't touch this!
	}

	public static final String START_OF_LINE = "^";
	public static final String END_OF_LINE = "$";
	public static final String END_OF_INPUT = "\\z";
	public static final String END_OF_LAST_MATCH = "\\G";

	public static Function<MatchInfo, Boolean> getFN(String boundIdent) {
		switch ( boundIdent ) {
			case START_OF_LINE:
				return CARET_FN;
			case END_OF_LINE:
				return DOLLAR_FN;
			case END_OF_INPUT:
				return END_OF_INPUT_FN;
			case END_OF_LAST_MATCH:
				return END_OF_LAST_MATCH_FN;
			default:
				throw new IllegalArgumentException( "boundIdent " + boundIdent + " not found." );
		}
	}

	public static Function<MatchInfo, Boolean> CARET_FN = (mi) -> {
		// Perl does not match ^ at end of input even after newline
		if ( mi.getPos() > mi.getWholeString().codePointLength() - 1 ) {
			return false;
		}
		if ( mi.getPos() == 0 ) {
			return true;
		}
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			int length = eff.codePointLength();
			CharSeq whole = mi.getWholeString();
			//zero-based position
			if ( mi.getPos() >= length ) {
				boolean eq = true;
				int charPos = 0;
				for ( int i = length; i > 0; --i ) {
					if ( whole.codePoint( mi.getPos() - i ) != eff.codePoint( charPos++ ) ) {
						eq = false;
						break;
					}
				}
				if ( eq ) {
					return true;
				}
			}
		}
		return false;
	};

	public static final Function<MatchInfo, Boolean> DOLLAR_FN = (mi) -> {
		//we are at the end, so match the dollar sign
		if ( mi.getPos() == mi.getWholeString().codePointLength() ) {
			return true;
		}
		//check if the following stuff is the end of input
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			int length = eff.codePointLength();
			CharSeq whole = mi.getWholeString();
			//zero-based position
			if ( mi.getPos() + length <= mi.getWholeString().codePointLength() ) {
				boolean eq = true;
				for ( int i = 0; i < length; ++i ) {
					if ( whole.codePoint( mi.getPos() + i ) != eff.codePoint( i ) ) {
						eq = false;
						break;
					}
				}
				if ( eq ) {
					return true;
				}
			}
		}
		return false;
	};

	public static final Function<MatchInfo, Boolean> END_OF_INPUT_FN = (mi) -> mi.getPos() == mi.getWholeString()
			.codePointLength();

	public static final Function<MatchInfo, Boolean> END_OF_LAST_MATCH_FN = (mi) ->
			mi.getLastMatch() == -1 || mi.getPos() == mi.getLastMatch();

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
