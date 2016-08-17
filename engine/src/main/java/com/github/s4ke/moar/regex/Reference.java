package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.moa.states.VariableState;

/**
 * @author Martin Braun
 */
final class Reference implements Regex {

	private final String reference;

	Reference(String reference) {
		this.reference = reference;
	}

	@Override
	public String toString() {
		return "\\k<" + this.reference + ">";
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
		if ( !variables.containsKey( this.reference ) ) {
			Variable var = new Variable( this.reference );
			variables.put( this.reference, var );
		}
		states.add( Moa.SRC );
		states.add( Moa.SNK );

		VariableState varState = new VariableState( idxSupplier.get(), this.reference );
		states.add( varState );
		selfRelevant.put( this, new HashMap<>() );
		selfRelevant.get( this ).put( this.reference, varState );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		VariableState varState = (VariableState) selfRelevant.get( this ).get( this.reference );
		edgeGraph.addEdgeWithDeterminismCheck( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, varState ), this );
		edgeGraph.addEdgeWithDeterminismCheck( varState, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ), this );
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		//no-op
	}
}
