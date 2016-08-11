package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.util.Range;

import static com.github.s4ke.moar.regex.CharacterClassesUtils.NON_WORD_CHARACTER_FN;
import static com.github.s4ke.moar.regex.CharacterClassesUtils.fromTo;

/**
 * @author Martin Braun
 */
public interface Regex extends StateContributor, EdgeContributor, VariableOccurence {

	Regex CARET = new BoundaryRegex(
			BoundConstants.START_OF_LINE, BoundConstants.CARET_FN
	);

	Regex DOLLAR = new BoundaryRegex(
			BoundConstants.END_OF_LINE, BoundConstants.DOLLAR_FN
	);

	Regex END_OF_INPUT = new BoundaryRegex(
			BoundConstants.END_OF_INPUT,
			BoundConstants.END_OF_INPUT_FN
	);

	Regex END_OF_LAST_MATCH = new BoundaryRegex(
			BoundConstants.END_OF_LAST_MATCH, BoundConstants.END_OF_LAST_MATCH_FN
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

	static Regex set(int from, int to) {
		return new SetRegex( fromTo( from, to ), "[" + from + "-" + to + "]" );
	}

	static Regex set(Range... ranges) {
		if ( ranges.length == 0 ) {
			throw new IllegalArgumentException();
		}
		StringBuilder stringRepresentation = new StringBuilder();
		stringRepresentation.append( "[" );
		for ( Range range : ranges ) {
			stringRepresentation.append( range.from ).append( "-" ).append( range.to );
		}
		stringRepresentation.append( "]" );
		return new SetRegex( CharacterClassesUtils.positiveFn( ranges ), stringRepresentation.toString() );
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
				CharacterClassesUtils.negativeFn( ranges ), stringRepresentation.toString()
		);
	}

	static Regex any_() {
		return new SetRegex( CharacterClassesUtils.ANY_FN, CharacterClassesUtils.ANY );
	}

	static Regex whiteSpace() {
		return new SetRegex( CharacterClassesUtils.WHITE_SPACE_FN, CharacterClassesUtils.WHITE_SPACE );
	}

	static Regex nonWhiteSpace() {
		return new SetRegex( CharacterClassesUtils.NON_WHITE_SPACE_FN, CharacterClassesUtils.NON_WHITE_SPACE );
	}

	static Regex digit() {
		return new SetRegex( CharacterClassesUtils.DIGIT_FN, CharacterClassesUtils.DIGIT );
	}

	static Regex nonDigit() {
		return new SetRegex( CharacterClassesUtils.NON_DIGIT_FN, CharacterClassesUtils.NON_DIGIT );
	}

	static Regex wordCharacter() {
		return new SetRegex(
				CharacterClassesUtils.WORD_CHARACTER_FN
				, CharacterClassesUtils.WORD_CHARACTER
		);
	}

	static Regex nonWordCharacter() {
		return new SetRegex(
				NON_WORD_CHARACTER_FN, CharacterClassesUtils.NON_WORD_CHARACTER
		);
	}

	static Regex set(String from, String to) {
		return set( from.codePointAt( 0 ), to.codePointAt( 0 ) );
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

}
