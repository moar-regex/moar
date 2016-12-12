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
package com.github.s4ke.moar.moa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.edgegraph.ActionType;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.util.CharSeq;
import com.github.s4ke.moar.util.IntCharSeq;


/**
 * Basic implementation of a Memory Occurence Automaton with support
 * for backreferences using Variable States.
 *
 * @author Martin Braun
 */
public final class Moa {

	public static final State SRC = EdgeGraph.SRC;
	public static final State SNK = EdgeGraph.SNK;

	private Map<String, Variable> vars = new HashMap<>();
	private EdgeGraph edges = new EdgeGraph();

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
		this.edges.freeze();
		if ( !this.edges.isDeterministic() ) {
			throw new NonDeterministicException( "this moa is not deterministic" );
		}
	}

	public void checkNotFrozen() {
		if ( this.frozen ) {
			throw new IllegalStateException( "this Moa is frozen" );
		}
		this.edges.checkNotFrozen();
	}

	public void checkFrozen() {
		if ( !this.frozen ) {
			throw new IllegalStateException( "this Moa is not frozen" );
		}
	}

	public EdgeGraph getEdges() {
		this.checkFrozen();
		return this.edges;
	}

	public void setEdges(EdgeGraph edges) {
		this.checkNotFrozen();
		this.edges = edges;
	}

	public static Set<MemoryAction> f(Set<MemoryAction> a1, Set<MemoryAction> a2) {
		Set<MemoryAction> ret = new HashSet<>();
		Set<String> variablesHandled = new HashSet<>();
		for ( MemoryAction ma : a2 ) {
			if ( ma.actionType == ActionType.OPEN || ma.actionType == ActionType.RESET ) {
				ret.add( ma );
				variablesHandled.add( ma.variable );
			}
		}
		for ( MemoryAction ma : a1 ) {
			if ( !variablesHandled.contains( ma.variable ) ) {
				if ( ma.actionType == ActionType.OPEN || ma.actionType == ActionType.RESET || ma.actionType == ActionType.CLOSE ) {
					ret.add( ma );
				}
			}
		}
		return ret;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.checkNotFrozen();
		this.vars = variables;
	}

	public Map<String, Variable> getVars() {
		return Collections.unmodifiableMap( this.vars );
	}

	public MoaMatcher matcher(CharSeq charSeq) {
		this.checkFrozen();
		Map<String, Variable> varCopy = new HashMap<>( this.vars.size() );
		for ( Map.Entry<String, Variable> entry : this.vars.entrySet() ) {
			varCopy.put( entry.getKey(), new Variable( entry.getValue() ) );
		}
		return new MoaMatcherImpl( this.edges, varCopy, charSeq );
	}

	public MoaMatcher matcher(CharSequence str) {
		return this.matcher( new IntCharSeq( str ) );
	}

	public boolean check(CharSequence str) {
		return this.matcher( str ).matches();
	}

	public boolean check(CharSeq charSeq) {
		return this.matcher( charSeq ).matches();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Moa moa = (Moa) o;

		if ( frozen != moa.frozen ) {
			return false;
		}
		if ( vars != null ? !vars.equals( moa.vars ) : moa.vars != null ) {
			return false;
		}
		return !(edges != null ? !edges.equals( moa.edges ) : moa.edges != null);

	}

	@Override
	public int hashCode() {
		int result = vars != null ? vars.hashCode() : 0;
		result = 31 * result + (edges != null ? edges.hashCode() : 0);
		result = 31 * result + (frozen ? 1 : 0);
		return result;
	}
}
