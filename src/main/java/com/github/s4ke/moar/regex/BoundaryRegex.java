package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.BoundState;
import com.github.s4ke.moar.moa.states.MatchInfo;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
public class BoundaryRegex implements Regex {

	private static final String SELF_RELEVANT_KEY = "";

	private final int boundHandled;
	private final Function<MatchInfo, Boolean> matchDescriptor;

	public BoundaryRegex(int boundHandled, Function<MatchInfo, Boolean> matchDescriptor) {
		this.boundHandled = boundHandled;
		this.matchDescriptor = matchDescriptor;
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
		State state = new BoundState( idxSupplier.get(), this.boundHandled, this.matchDescriptor );
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

	@Override
	public Regex copy() {
		return new BoundaryRegex( this.boundHandled, this.matchDescriptor );
	}
}
