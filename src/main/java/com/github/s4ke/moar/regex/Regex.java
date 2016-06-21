package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;

/**
 * @author Martin Braun
 */
public interface Regex extends StateContributor, EdgeContributor {

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
				ret = new Primitive( new Symbol( String.valueOf( ch ), null ) );
			}
			else {
				ret = ret.and( new Primitive( new Symbol( String.valueOf( ch ), null ) ) );
			}
		}
		if ( ret == null ) {
			return eps();
		}
		return ret;
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

	default Regex bind(String name) {
		return new Binding( name, this.copy() );
	}

	default Regex build() {
		Regex copy = this.copy();
		copy.build( new HashMap<>(), new HashMap<>() );
		return copy;
	}

	default Moa toMoa() {
		Moa moa = new Moa();
		Map<String, Variable> variables = new HashMap<>();
		Set<State> states = new HashSet<>();
		Map<Regex, Map<String, State>> selfRelevant = new HashMap<>();
		AtomicInteger idStart = new AtomicInteger( 2 );
		Supplier<Integer> idxSupplier = idStart::getAndIncrement;
		this.contributeStates( variables, states, selfRelevant, idxSupplier );
		EdgeGraph edgeGraph = new EdgeGraph();
		for(State state : states) {
			edgeGraph.addState(state);
		}
		this.contributeEdges( edgeGraph, variables, states, selfRelevant );
		moa.setVariables( variables );
		moa.setEdges( edgeGraph );
		moa.freeze();
		return moa;
	}

	Regex copy();

	String toString();

	void build(Map<String, Integer> strCount, Map<String, Regex> bindings);


}
