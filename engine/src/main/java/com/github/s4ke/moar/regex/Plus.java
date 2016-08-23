/*
 The MIT License (MIT)

 Copyright (c) 2016 Martin Braun

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.github.s4ke.moar.regex;

import java.util.ArrayList;
import java.util.Collection;
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
		return this.regex.toString() + "+";
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

		Collection<EdgeGraph.Edge> srcEdges = edgeGraph.getEdges( Moa.SRC );

		//used to avoid concurrent modification
		List<Supplier<Void>> addActions = new ArrayList<>();

		for ( State toSnkState : states ) {
			for ( EdgeGraph.Edge snkEdge : edgeGraph.getEdges( toSnkState ) ) {
				if ( snkEdge.destination.equals( Moa.SNK.getIdx() ) ) {
					//now we are a real SNK edge
					for ( EdgeGraph.Edge srcEdge : srcEdges ) {
						Integer fromSrcState = srcEdge.destination;
						Supplier<Void> val = () -> {
							edgeGraph.addEdgeWithDeterminismCheck(
									toSnkState, new EdgeGraph.Edge(
											Moa.f(
													snkEdge.memoryAction,
													srcEdge.memoryAction
											), fromSrcState
									),
									this
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
