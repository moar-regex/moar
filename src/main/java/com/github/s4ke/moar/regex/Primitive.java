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
	private boolean built = false;

	Primitive(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return this.symbol.toString();
	}

	@Override
	public void build(
			Map<String, Integer> strCount, Map<String, Regex> bindings) {
		if ( this.built ) {
			throw new IllegalStateException( "this regex already has been built" );
		}
		Integer curCount = strCount.get( this.symbol.symbol );
		if ( curCount == null ) {
			curCount = 0;
		}
		this.symbol = new Symbol( this.symbol.symbol, curCount );
		strCount.put( this.symbol.symbol, curCount + 1 );
		this.built = true;
	}

	@Override
	public Regex copy() {
		return new Primitive( new Symbol( this.symbol.symbol, this.symbol.number ) );
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
