package com.github.s4ke.moar.regex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.github.s4ke.moar.strings.EfficientString;
import com.github.s4ke.moar.util.Range;

/**
 * @author Martin Braun
 */
public class CharacterClassesUtils {

	private static final Set<Character> WHITE_SPACE_CHARS = new HashSet<>(
			Arrays.asList(
					' ',
					'\t',
					'\n',
					(char) 0x0B,
					'\f',
					'\r'
			)
	);

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

	public static Function<EfficientString, Boolean> getFn(String identifier) {
		switch (identifier) {
			case ANY:
				return ANY_FN;
			case WHITE_SPACE:
				return WHITE_SPACE_FN;
			case NON_WHITE_SPACE:
				return NON_WORD_CHARACTER_FN;
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

	public static final Function<EfficientString, Boolean> ANY_FN = (string) -> string.length() == 1;

	public static final Function<EfficientString, Boolean> WHITE_SPACE_FN = str -> str.length() == 1 && WHITE_SPACE_CHARS
			.contains( str.charAt( 0 ) );

	public static final Function<EfficientString, Boolean> NON_WHITE_SPACE_FN = str -> str.length() == 1 && !WHITE_SPACE_CHARS
			.contains( str.charAt( 0 ) );

	public static final Function<EfficientString, Boolean> DIGIT_FN = str -> str.length() == 1 && Character.isDigit(
			str.charAt(
					0
			)
	);

	public static final Function<EfficientString, Boolean> NON_DIGIT_FN = str -> str.length() == 1 && !Character.isDigit(
			str.charAt( 0 )
	);

	public static final Function<EfficientString, Boolean> WORD_CHARACTER_FN = str -> str.length() == 1 && (fromTo(
			'a',
			'z'
	).apply( str ) || fromTo( 'A', 'Z' ).apply(
			str
	) || fromTo( '0', '9' ).apply( str ) || fromTo( '_', '_' ).apply( str ));

	public static final Function<EfficientString, Boolean> NON_WORD_CHARACTER_FN = str -> str.length() == 1 && !fromTo(
			'a',
			'z'
	).apply( str ) && !fromTo( 'A', 'Z' ).apply( str )
			&& !fromTo( '0', '9' ).apply( str ) && str.charAt( 0 ) != '_';

	static Function<EfficientString, Boolean> fromTo(char from, char to) {
		return (str) ->
				str.length() == 1 && str.charAt( 0 ) >= from && str.charAt( 0 ) <= to;
	}

	public static Function<EfficientString, Boolean> negativeFn(Range[] ranges) {
		return (EfficientString str) -> {
			if ( str.length() != 1 ) {
				return false;
			}
			for ( Range range : ranges ) {
				if ( str.charAt( 0 ) >= range.from && str.charAt( 0 ) <= range.to ) {
					return false;
				}
			}
			return true;
		};
	}

	public static Function<EfficientString, Boolean> positiveFn(Range[] ranges) {
		return (EfficientString str) -> {
			if ( str.length() != 1 ) {
				return false;
			}
			for ( Range range : ranges ) {
				if ( str.charAt( 0 ) >= range.from && str.charAt( 0 ) <= range.to ) {
					return true;
				}
			}
			return false;
		};
	}

}
