package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
final class Choice implements Regex {

	private final Regex fst;
	private final Regex snd;

	public Choice(Regex fst, Regex snd) {
		this.fst = fst;
		this.snd = snd;
	}

	@Override
	public String toString() {
		return "{" + this.fst.toString() + "{or}" + this.snd.toString() + "}";
	}

	@Override
	public Regex copy() {
		return new Choice( this.fst.copy(), this.snd.copy() );
	}

	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		this.fst.contributeStates( variables, states, selfRelevant, idxSupplier );
		this.snd.contributeStates( variables, states, selfRelevant, idxSupplier );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		this.fst.contributeEdges( edgeGraph, variables, states, selfRelevant );
		this.snd.contributeEdges( edgeGraph, variables, states, selfRelevant );
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		this.fst.calculateVariableOccurences( variables, varIdxSupplier );
		this.snd.calculateVariableOccurences( variables, varIdxSupplier );
	}
}
