package com.github.s4ke.moar.moa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.edgegraph.ActionType;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;


/**
 * @author Martin Braun
 */
public final class Moa {

	public static final State SRC = EdgeGraph.SRC;
	public static final State SNK = EdgeGraph.SNK;

	private Map<String, Variable> vars = new HashMap<>();
	private EdgeGraph edges = new EdgeGraph();

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
		this.edges.freeze();
		if ( !this.edges.isDeterministic() ) {
			throw new NonDeterministicException( "this moa is not deterministic" );
		}
	}

	public boolean isDeterministic() {
		return this.edges.isDeterministic();
	}

	public void checkNotFrozen() {
		if ( this.frozen ) {
			throw new IllegalStateException( "this Moa is frozen" );
		}
		this.edges.checkNotFrozen();
	}

	public void checkFrozen() {
		if ( !this.frozen ) {
			throw new IllegalStateException( "this Moa is not frozen" );
		}
	}

	public void setEdges(EdgeGraph edges) {
		this.checkNotFrozen();
		this.edges = edges;
	}

	public static Set<MemoryAction> f(Set<MemoryAction> a1, Set<MemoryAction> a2) {
		Set<MemoryAction> ret = new HashSet<>();
		Set<String> variablesHandled = new HashSet<>();
		for ( MemoryAction ma : a2 ) {
			if ( ma.actionType == ActionType.OPEN || ma.actionType == ActionType.RESET ) {
				ret.add( ma );
				variablesHandled.add( ma.variable );
			}
		}
		for ( MemoryAction ma : a1 ) {
			if ( !variablesHandled.contains( ma.variable ) ) {
				if ( ma.actionType == ActionType.OPEN || ma.actionType == ActionType.RESET || ma.actionType == ActionType.CLOSE ) {
					ret.add( ma );
					variablesHandled.add( ma.variable );
				}
			}
		}
		return ret;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.checkNotFrozen();
		this.vars = variables;
	}

	public MoaMatcher matcher(CharSequence str) {
		this.checkFrozen();
		Map<String, Variable> varCopy = new HashMap<>( this.vars.size() );
		for ( Map.Entry<String, Variable> entry : this.vars.entrySet() ) {
			varCopy.put( entry.getKey(), new Variable( entry.getValue() ) );
		}
		return new MoaMatcher( this.edges, varCopy, str );
	}

	public boolean check(String str) {
		return this.matcher( str ).checkAsSingleWord();
	}


}
