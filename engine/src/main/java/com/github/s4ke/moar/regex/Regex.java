package com.github.s4ke.moar.regex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public interface Regex extends StateContributor, EdgeContributor, VariableOccurence {

	Set<Character> WHITE_SPACE_CHARS = new HashSet<>( Arrays.asList( ' ', '\t', '\n', (char) 0x0B, '\f', '\r' ) );

	Regex CARET = new BoundaryRegex(
			BoundConstants.START_OF_LINE, (mi) -> {
		// Perl does not match ^ at end of input even after newline
		if ( mi.getPos() > mi.getWholeString().length() - 1 ) {
			return false;
		}
		if ( mi.getPos() == 0 ) {
			return true;
		}
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			int length = eff.length();
			CharSequence whole = mi.getWholeString();
			//zero-based position
			if ( mi.getPos() >= length ) {
				boolean eq = true;
				int charPos = 0;
				for ( int i = length; i > 0; --i ) {
					if ( whole.charAt( mi.getPos() - i ) != eff.charAt( charPos++ ) ) {
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
	}
	);

	Regex DOLLAR = new BoundaryRegex(
			BoundConstants.END_OF_LINE, (mi) -> {
		//we are at the end, so match the dollar sign
		if ( mi.getPos() == mi.getWholeString().length() ) {
			return true;
		}
		//check if the following stuff is the end of input
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			int length = eff.length();
			CharSequence whole = mi.getWholeString();
			//zero-based position
			if ( mi.getPos() + length <= mi.getWholeString().length() ) {
				boolean eq = true;
				for ( int i = 0; i < length; ++i ) {
					if ( whole.charAt( mi.getPos() + i ) != eff.charAt( i ) ) {
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
	}
	);

	Regex END_OF_INPUT = new BoundaryRegex(
			BoundConstants.END_OF_INPUT,
			(mi) -> mi.getPos() == mi.getWholeString().length()
	);

	Regex END_OF_LAST_MATCH = new BoundaryRegex(
			BoundConstants.END_OF_LAST_MATCH, (mi) ->
			mi.getLastMatch() == -1 || mi.getPos() == mi.getLastMatch()
	);

	static Regex caret() {
		return CARET;
	}

	static Regex dollar_() {
		return DOLLAR;
	}

	static Regex end_() {
		return END_OF_INPUT;
	}

	static Regex endOfLastMatch() {
		return END_OF_LAST_MATCH;
	}

	static Regex reference(String reference) {
		return new Reference( reference );
	}

	static Regex eps() {
		return Epsilon.INSTANCE;
	}

	static Regex str(String str) {
		Regex ret = null;
		for ( char ch : str.toCharArray() ) {
			if ( ret == null ) {
				ret = new Primitive( new Symbol( String.valueOf( ch ) ) );
			}
			else {
				ret = ret.and( new Primitive( new Symbol( String.valueOf( ch ) ) ) );
			}
		}
		if ( ret == null ) {
			return Regex.eps();
		}
		return ret;
	}

	static Regex set(char from, char to) {
		return new SetRegex( fromTo( from, to ), "[" + from + "-" + to + "]" );
	}

	static Regex set(Range... ranges) {
		if ( ranges.length == 0 ) {
			throw new IllegalArgumentException();
		}
		Regex regex = set( ranges[0].from, ranges[0].to );
		for ( int i = 1; i < ranges.length; ++i ) {
			regex.or( set( ranges[i].from, ranges[i].to ) );
		}
		return regex;
	}

	static Regex negativeSet(final Range... ranges) {
		if ( ranges.length == 0 ) {
			throw new IllegalArgumentException( "ranges.length was equal to zero" );
		}
		StringBuilder stringRepresentation = new StringBuilder();
		stringRepresentation.append( "[^" );
		for ( Range range : ranges ) {
			stringRepresentation.append( range.from ).append( "-" ).append( range.to );
		}
		stringRepresentation.append( "]" );
		return new SetRegex(
				(EfficientString str) -> {
					if ( str.length() != 1 ) {
						return false;
					}
					for ( Range range : ranges ) {
						if ( str.charAt( 0 ) >= range.from && str.charAt( 0 ) <= range.to ) {
							return false;
						}
					}
					return true;
				}, stringRepresentation.toString()
		);
	}

	static Function<EfficientString, Boolean> fromTo(char from, char to) {
		return (str) ->
				str.length() == 1 && str.charAt( 0 ) >= from && str.charAt( 0 ) <= to;
	}

	static Regex any_() {
		return new SetRegex( (string) -> string.length() == 1, "." );
	}

	static Regex whiteSpace() {
		return new SetRegex( str -> str.length() == 1 && WHITE_SPACE_CHARS.contains( str.charAt( 0 ) ), "\\s" );
	}

	static Regex nonWhiteSpace() {
		return new SetRegex( str -> str.length() == 1 && !WHITE_SPACE_CHARS.contains( str.charAt( 0 ) ), "\\S" );
	}

	static Regex digit() {
		return new SetRegex( str -> str.length() == 1 && Character.isDigit( str.charAt( 0 ) ), "\\d" );
	}

	static Regex nonDigit() {
		return new SetRegex( str -> str.length() == 1 && !Character.isDigit( str.charAt( 0 ) ), "\\D" );
	}

	static Regex wordCharacter() {
		return new SetRegex(
				str -> str.length() == 1 && (fromTo( 'a', 'z' ).apply( str ) || fromTo( 'A', 'Z' ).apply(
						str
				) || fromTo( '0', '9' ).apply( str ) || fromTo( '_', '_' ).apply( str )), "\\w"
		);
	}

	static Regex nonWordCharacter() {
		return new SetRegex(
				str -> str.length() == 1 && !fromTo( 'a', 'z' ).apply( str ) && !fromTo( 'A', 'Z' ).apply( str )
						&& !fromTo( '0', '9' ).apply( str ) && str.charAt( 0 ) != '_', "\\W"
		);
	}

	static Regex set(String from, String to) {
		return set( from.charAt( 0 ), to.charAt( 0 ) );
	}

	default Regex or(Regex other) {
		return new Choice( this.copy(), other.copy() );
	}

	default Regex or(String other) {
		return new Choice( this.copy(), str( other ) );
	}

	default Regex and(Regex other) {
		return new Concat( this.copy(), other.copy() );
	}

	default Regex and(String other) {
		return new Concat( this.copy(), str( other ) );
	}

	default Regex plus() {
		return new Plus( this.copy() );
	}

	default Regex star() {
		return this.plus().or( Regex.eps() );
	}

	default Regex bind(String name) {
		return new Binding( name, this.copy() );
	}

	default Regex dollar() {
		return this.and( dollar_() );
	}

	default Regex end() {
		return this.and( end_() );
	}

	default Regex any() {
		return this.and( any_() );
	}

	//TODO: this can be done with a Stack and some clever handling
	//instead of recursion
	default Moa toMoa() {
		Moa moa = new Moa();
		Map<String, Variable> variables = new HashMap<>();
		Set<State> states = new HashSet<>();
		Map<Regex, Map<String, State>> selfRelevant = new HashMap<>();
		AtomicInteger stateIdxStart = new AtomicInteger( 2 );
		Supplier<Integer> idxSupplier = stateIdxStart::getAndIncrement;
		this.contributeStates( variables, states, selfRelevant, idxSupplier );
		EdgeGraph edgeGraph = new EdgeGraph();
		for ( State state : states ) {
			edgeGraph.addState( state );
		}
		this.contributeEdges( edgeGraph, variables, states, selfRelevant );
		//we start at 1 for variables just like Java Regexes
		AtomicInteger varIdxStart = new AtomicInteger( 1 );
		this.calculateVariableOccurences( variables, varIdxStart::getAndIncrement );
		moa.setVariables( variables );
		moa.setEdges( edgeGraph );
		if ( !moa.isDeterministic() ) {
			throw new NonDeterministicException( this.toString() + " is not deterministic" );
		}
		moa.freeze();
		return moa;
	}

	Regex copy();

	String toString();

	class Range {

		private final char from;
		private final char to;

		private Range(char from, char to) {
			this.from = from;
			this.to = to;
		}

		public static Range of(char from, char to) {
			return new Range( from, to );
		}

	}

}
