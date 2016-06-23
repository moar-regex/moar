package com.github.s4ke.moar.regex;

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
final class Binding implements Regex {

	private final String name;
	private final Regex regex;

	Binding(String name, Regex regex) {
		this.name = name;
		this.regex = regex;
	}

	@Override
	public String toString() {
		return "{Binding{" + this.name + ", " + this.regex.toString() + "}}";
	}

	@Override
	public Regex copy() {
		return new Binding( this.name, this.regex.copy() );
	}

	@Override
	public void contributeStates(
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant,
			Supplier<Integer> idxSupplier) {
		this.regex.contributeStates( variables, states, selfRelevant, idxSupplier );
	}

	@Override
	public void contributeEdges(
			EdgeGraph edgeGraph,
			Map<String, Variable> variables,
			Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant) {
		if ( !variables.containsKey( this.name ) ) {
			Variable var = new Variable( name );
			variables.put( name, var );
		}

		this.regex.contributeEdges( edgeGraph, variables, states, selfRelevant );

		for ( EdgeGraph.Edge edge : edgeGraph.getEdges( Moa.SRC ) ) {
			if ( edge.destination != Moa.SNK.getIdx() ) {
				edge.memoryAction.add( new MemoryAction( ActionType.OPEN, this.name ) );
			}
			else {
				edge.memoryAction.add( new MemoryAction( ActionType.RESET, this.name ) );
			}
		}
		edgeGraph.getStates().stream().filter( state -> state != Moa.SRC ).forEach(
				state -> {
					edgeGraph.getEdges( state ).stream().filter( edge -> edge.destination == Moa.SNK.getIdx() ).forEach(
							edge -> {
								edge.memoryAction.add( new MemoryAction( ActionType.CLOSE, this.name ) );
							}
					);
				}
		);
	}
}
