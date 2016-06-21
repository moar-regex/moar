package com.github.s4ke.moar.regex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.ActionType;
import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.MemoryAction;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;

/**
 * @author Martin Braun
 */
final class Epsilon implements Regex {

	private Epsilon() {

	}

	public static final Epsilon INSTANCE = new Epsilon();

	@Override
	public String toString() {
		return "{epsilon}";
	}

	@Override
	public void build(
			Map<String, Integer> strCount, Map<String, Regex> bindings) {
		//no-op
	}

	@Override
	public Regex copy() {
		return this;
	}

	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		states.add( Moa.SRC );
		states.add( Moa.SNK );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		edgeGraph.addEdge( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ) );
	}
}
