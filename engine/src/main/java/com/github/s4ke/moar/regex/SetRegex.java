package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.SetState;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
class SetRegex implements Regex {

	private static final String SELF_RELEVANT_KEY = "";

	private final Function<EfficientString, Boolean> setDescriptor;
	private final String stringRepresentation;

	public SetRegex(Function<EfficientString, Boolean> setDescriptor, String stringRepresentation) {
		this.setDescriptor = setDescriptor;
		this.stringRepresentation = stringRepresentation;
	}

	@Override
	public Regex copy() {
		return new SetRegex( this.setDescriptor, this.stringRepresentation );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		State state = selfRelevant.get( this ).get( SELF_RELEVANT_KEY );
		edgeGraph.addEdge( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, state ) );
		edgeGraph.addEdge( state, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ) );
	}

	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		//we default to length 1
		State state = new SetState( idxSupplier.get(), 1, this.setDescriptor, stringRepresentation );

		states.add( state );
		states.add( Moa.SRC );
		states.add( Moa.SNK );
		selfRelevant.put( this, new HashMap<>() );
		selfRelevant.get( this ).put( SELF_RELEVANT_KEY, state );
	}

	@Override
	public void calculateVariableOccurences(
			Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {

	}
}
