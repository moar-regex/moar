package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.BasicState;
import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.MemoryAction;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;

/**
 * @author Martin Braun
 */
final class Primitive implements Regex {

	private Symbol symbol;

	Primitive(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return this.symbol.toString();
	}

	@Override
	public Regex copy() {
		return new Primitive( new Symbol( this.symbol.symbol ) );
	}

	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		State state = new BasicState( idxSupplier.get(), this.symbol.symbol );
		states.add( state );
		states.add( Moa.SRC );
		states.add( Moa.SNK );
		selfRelevant.put( this, new HashMap<>() );
		selfRelevant.get( this ).put( this.symbol.symbol, state );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		State state = selfRelevant.get( this ).get( this.symbol.symbol );
		edgeGraph.addEdge( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, state ) );
		edgeGraph.addEdge( state, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ) );
	}
}
