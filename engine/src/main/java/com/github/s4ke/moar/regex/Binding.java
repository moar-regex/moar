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

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.ActionType;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

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
		return "(?<" + this.name + ">" + this.regex.toString() + ")";
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

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		Variable variable = variables.get( this.name );
		variable.setOccurenceInRegex( varIdxSupplier.get() );

		this.regex.calculateVariableOccurences( variables, varIdxSupplier );
	}

}
