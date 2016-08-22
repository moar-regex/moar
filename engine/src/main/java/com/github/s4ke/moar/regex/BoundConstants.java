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
