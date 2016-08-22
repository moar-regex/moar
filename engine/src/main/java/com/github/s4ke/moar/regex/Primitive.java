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

import com.github.s4ke.moar.moa.states.BasicState;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

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
		edgeGraph.addEdgeWithDeterminismCheck( Moa.SRC, new EdgeGraph.Edge( MemoryAction.NO_OP, state ), this );
		edgeGraph.addEdgeWithDeterminismCheck( state, new EdgeGraph.Edge( MemoryAction.NO_OP, Moa.SNK ), this );
	}

	@Override
	public void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier) {
		//no-op
	}
}
