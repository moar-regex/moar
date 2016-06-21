package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;

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
	public void build(
			Map<String, Integer> strCount, Map<String, Regex> bindings) {
		this.fst.build( strCount, bindings );
		this.snd.build( strCount, bindings );
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
}
