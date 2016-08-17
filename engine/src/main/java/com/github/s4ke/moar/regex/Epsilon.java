package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

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
		edgeGraph.addEdgeWithDeterminismCheck( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ), this );
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		//no-op
	}
}
