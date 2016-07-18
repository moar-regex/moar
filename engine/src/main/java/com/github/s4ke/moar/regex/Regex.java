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

	static Regex set(Function<EfficientString, Boolean> setDescriptor) {
		return new SetRegex( setDescriptor );
	}

	static Regex set(char from, char to) {
		return set(
				fromTo( from, to )
		);
	}

	static Function<EfficientString, Boolean> fromTo(char from, char to) {
		return (str) ->
				str.length() == 1 && str.charAt( 0 ) >= from && str.charAt( 0 ) <= to;
	}

	static Regex whiteSpace() {
		return set( str -> str.length() == 1 && WHITE_SPACE_CHARS.contains( str.charAt( 0 ) ) );
	}

	static Regex nonWhiteSpace() {
		return set( str -> str.length() == 1 && !WHITE_SPACE_CHARS.contains( str.charAt( 0 ) ) );
	}

	static Regex digit() {
		return set( str -> str.length() == 1 && Character.isDigit( str.charAt( 0 ) ) );
	}

	static Regex nonDigit() {
		return set( str -> str.length() == 1 && !Character.isDigit( str.charAt( 0 ) ) );
	}

	static Regex wordCharacter() {
		return set( 'a', 'z' ).or( set( 'A', 'Z' ) ).or( set( '0', '9' ) ).or( "_" );
	}

	static Regex nonWordCharacter() {
		return set(
				str -> str.length() == 1 && !fromTo( 'a', 'z' ).apply( str ) && !fromTo( 'A', 'Z' ).apply( str )
						&& !fromTo( '0', '9' ).apply( str ) && str.charAt( 0 ) != '_'
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

}
