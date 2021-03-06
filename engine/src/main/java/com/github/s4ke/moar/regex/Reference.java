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
