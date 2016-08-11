package com.github.s4ke.moar.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
final class Plus implements Regex {

	private final Regex regex;

	Plus(Regex regex) {
		this.regex = regex;
	}

	@Override
	public String toString() {
		return "{" + this.regex.toString() + "{+}" + "}";
	}

	@Override
	public Regex copy() {
		return new Plus( this.regex.copy() );
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
		this.regex.contributeEdges( edgeGraph, variables, states, selfRelevant );

		List<EdgeGraph.Edge> srcEdges = edgeGraph.getEdges( Moa.SRC );

		//used to avoid concurrent modification
		List<Supplier<Void>> addActions = new ArrayList<>();

		for ( State toSnkState : states ) {
			for ( EdgeGraph.Edge snkEdge : edgeGraph.getEdges( toSnkState ) ) {
				if ( snkEdge.destination.equals( Moa.SNK.getIdx() ) ) {
					//now we are a real SNK edge
					for ( EdgeGraph.Edge srcEdge : srcEdges ) {
						Integer fromSrcState = srcEdge.destination;
						Supplier<Void> val = () -> {
							edgeGraph.addEdge(
									toSnkState, new EdgeGraph.Edge(
											Moa.f(
													snkEdge.memoryAction,
													srcEdge.memoryAction
											), fromSrcState
									)
							);
							return null;
						};
						addActions.add( val );
					}
				}
			}
		}

		addActions.forEach( Supplier::get );
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		this.regex.calculateVariableOccurences( variables, varIdxSupplier );
	}
}
