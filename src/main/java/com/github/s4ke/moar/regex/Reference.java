package com.github.s4ke.moar.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.MemoryAction;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;
import com.github.s4ke.moar.moa.VariableState;

/**
 * @author Martin Braun
 */
final class Reference implements Regex {

	private final String reference;
	private Regex regex;
	private boolean built = false;

	Reference(String reference) {
		this.reference = reference;
	}

	@Override
	public String toString() {
		return "{Reference{" + this.reference + "," + this.regex.toString() + "}";
	}

	@Override
	public void build(
			Map<String, Integer> strCount, Map<String, Regex> bindings) {
		if ( this.built ) {
			throw new IllegalStateException( "this regex already has been built" );
		}
		Regex binding = bindings.get( this.reference );
		if ( binding == null ) {
			throw new IllegalArgumentException( "a binding with name " + this.reference + " does not exist (yet)!!" );
		}
		this.regex = binding;
		this.built = true;
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

		Variable var = variables.get( this.reference );
		VariableState varState = new VariableState( idxSupplier.get(), var );
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
		edgeGraph.addEdge( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, varState ) );
		edgeGraph.addEdge( varState, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ) );
	}
}
