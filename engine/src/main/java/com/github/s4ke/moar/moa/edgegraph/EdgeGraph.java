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
package com.github.s4ke.moar.moa.edgegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.states.BasicState;
import com.github.s4ke.moar.moa.states.BoundState;
import com.github.s4ke.moar.moa.states.MatchInfo;
import com.github.s4ke.moar.moa.states.SetState;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.moa.states.VariableState;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * The actual graph representation of a Memory Occurence Automaton.
 * Uses some special states for lower memory usage and real-world
 * implementation of theoretical concepts.
 *
 * Supported Types of states are:
 *
 * <ul>
 *     <li>{@link BasicState}</li>
 *     <li>{@link BoundState}</li>
 *     <li>{@link SetState}</li>
 *     <li>{@link VariableState}</li>
 * </ul>
 *
 * @author Martin Braun
 */
public final class EdgeGraph {

	public static final State SRC = new BasicState( 0, "SRC" ) {
		@Override
		public String toString() {
			return "SRC";
		}
	};
	//SNK's name must be equal to "" so that epsilon works
	public static final State SNK = new BasicState( 1, "" ) {
		@Override
		public String toString() {
			return "SNK";
		}
	};

	private final Map<Integer, State> states = new HashMap<>();
	private final Map<Integer, List<Edge>> edges = new HashMap<>();
	private final Map<Integer, List<Edge>> setEdges = new HashMap<>();
	private final Map<Integer, List<Edge>> boundEdges = new HashMap<>();
	private final Map<Integer, Map<EfficientString, Edge>> staticEdges = new HashMap<>();
	private final Map<Integer, List<Edge>> backRefOrEpsilonEdges = new HashMap<>();

	private boolean frozen = false;

	public EdgeGraph copy() {
		EdgeGraph edgeGraph = new EdgeGraph();
		for ( State state : this.getStates() ) {
			edgeGraph.addState( state );
		}
		for ( State state : this.getStates() ) {
			for ( EdgeGraph.Edge edge : this.getEdges( state ) ) {
				EdgeGraph.Edge edgeCpy = new EdgeGraph.Edge( new HashSet<>( edge.memoryAction ), edge.destination );
				edgeGraph.addEdge( state, edgeCpy );
			}
		}
		return edgeGraph;
	}

	public void freeze() {
		this.frozen = true;
		for ( Map.Entry<Integer, List<Edge>> entry : this.edges.entrySet() ) {
			if ( backRefOrEpsilonEdges.get( entry.getKey() ).size() > 2 ) {
				throw new AssertionError( "backRfOrEpsilonEdges had size greater 2 for state " + entry.getKey() );
			}
		}
	}

	public void checkNotFrozen() {
		if ( this.frozen ) {
			throw new IllegalStateException( "this EdgeGraph is frozen" );
		}
	}

	public static class Edge {
		public Edge(MemoryAction memoryAction, State destination) {
			this.memoryAction = new HashSet<>();
			if ( memoryAction != null ) {
				this.memoryAction.add( memoryAction );
			}
			this.destination = destination.getIdx();
		}

		public Edge(Set<MemoryAction> memoryActionSet, State toState) {
			this( memoryActionSet, toState.getIdx() );
		}

		public Edge(Set<MemoryAction> memoryAction, Integer destination) {
			this.memoryAction = memoryAction;
			this.destination = destination;
		}

		public Set<MemoryAction> memoryAction;
		public final Integer destination;

		public void freeze() {
			this.memoryAction = Collections.unmodifiableSet( this.memoryAction );
		}

		@Override
		public String toString() {
			return "Edge{" +
					"memoryAction=" + memoryAction +
					", destination=" + destination +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Edge edge = (Edge) o;

			if ( memoryAction != null ? !memoryAction.equals( edge.memoryAction ) : edge.memoryAction != null ) {
				return false;
			}
			return !(destination != null ? !destination.equals( edge.destination ) : edge.destination != null);

		}

	}

	public Edge getEdge(State from, MatchInfo matchInfo, Map<String, Variable> variables) {
		if ( this.boundEdges.get( from.getIdx() ).size() >= 1 ) {
			if ( this.boundEdges.get( from.getIdx() ).size() > 1 ) {
				throw new NonDeterministicException( "multiple boundEdges found in graph" );
			}
			else {
				Edge edge = this.boundEdges.get( from.getIdx() ).iterator().next();
				BoundState state = (BoundState) this.states.get( edge.destination );
				if ( state.canConsume( matchInfo ) ) {
					return edge;
				}
			}
		}

		{
			Map<EfficientString, Edge> staticEdges = this.staticEdges.get( from.getIdx() );
			if ( staticEdges != null ) {
				Edge edge = staticEdges.get( matchInfo.getString() );
				if ( edge != null ) {
					return edge;
				}
			}
		}

		{
			List<Edge> setEdges = this.setEdges.get( from.getIdx() );
			if ( setEdges != null ) {
				for ( Edge edge : setEdges ) {
					State state = this.states.get( edge.destination );
					if ( state.isSet() && state.canConsume( matchInfo.getString() ) ) {
						return edge;
					}
				}
			}
		}

		List<Edge> set = this.edges.get( from.getIdx() );
		if ( set == null ) {
			return null;
		}

		//this is only reached for the case that we
		//are in a state with a backreference
		//in this case we have at most 2 edges (the backref or possibly epsilon)
		//so this doesn't change the overall runtime
		Edge edgeRes = null;
		for ( Edge edge : this.backRefOrEpsilonEdges.get( from.getIdx() ) ) {
			State state = this.states.get( edge.destination );
			if ( state.isVariable() && state.getEdgeString( variables ).equalTo( matchInfo.getString() ) ) {
				if ( edgeRes != null ) {
					throw new IllegalStateException(
							"non-determinism detected, multiple edges for string: " + matchInfo.getString() + ". The edges were: " + set
					);
				}
				edgeRes = edge;
			}
		}
		return edgeRes;
	}

	public enum StepResult {
		CONSUMED,
		NOT_CONSUMED,
		REJECTED
	}

	public StepResult step(CurStateHolder stateHolder, MatchInfo mi, Map<String, Variable> vars) {
		{
			EdgeGraph.Edge edge = this.getEdge( stateHolder.getState(), mi, vars );
			if ( edge != null ) {
				State destinationState = this.states.get( edge.destination );
				edge.memoryAction.forEach( memoryAction -> memoryAction.act( vars ) );
				//we have found an edge so we can accept this input
				vars.values().stream().filter( Variable::canConsume ).forEach(
						var -> var.consume( mi.getString() )
				);
				stateHolder.setState( destinationState );
				destinationState.touch();
				if ( destinationState.isBound() ) {
					return StepResult.NOT_CONSUMED;
				}
				return StepResult.CONSUMED;
			}
		}
		return StepResult.REJECTED;
	}

	/**
	 * checks determinism in a single state
	 */
	public boolean checkDeterminism(State state) {
		List<Edge> edges = this.edges.get( state.getIdx() );
		int edgeCnt = edges.size();

		VariableState variableDestinationState = null;
		BoundState boundState = null;
		Map<EfficientString, State> staticDestinationStates = new HashMap<>( edgeCnt );
		int staticOrSetCount;

		{
			Set<EfficientString> staticEdgeStrings = new HashSet<>();
			Map<State, AtomicInteger> destinationStateCount = new HashMap<>();

			//check if there are edges to different states with the same
			//terminal
			for ( Edge edge : edges ) {
				State destinationState = this.states.get( edge.destination );

				if ( destinationStateCount.computeIfAbsent(
						destinationState,
						(key) -> new AtomicInteger( 0 )
				)
						.incrementAndGet() >= 2 ) {
					return false;
				}

				if ( destinationState.isVariable() ) {
					if ( variableDestinationState != null ) {
						return false;
					}
					variableDestinationState = (VariableState) destinationState;
					continue;
				}

				if ( destinationState.isStatic() ) {
					//only called for terminals, so null is fine
					EfficientString edgeString = destinationState.getEdgeString( null );
					staticEdgeStrings.add( edgeString );
					State knownDestinationState = staticDestinationStates.get( edgeString );
					if ( knownDestinationState != null && knownDestinationState != destinationState ) {
						//duplicated edge to
						return false;
					}
					staticDestinationStates.put( edgeString, destinationState );
				}

				if ( destinationState.isBound() ) {
					if ( boundState != null ) {
						return false;
					}
					boundState = (BoundState) destinationState;
				}
			}

			staticOrSetCount = staticDestinationStates.size();

			//check if the sets contain values from the static states
			{
				List<Edge> setEdges = this.setEdges.get( state.getIdx() );
				if ( setEdges != null ) {
					for ( Edge edge : setEdges ) {
						State destinationState = this.states.get( edge.destination );
						if ( destinationState.isSet() ) {
							for ( EfficientString edgeString : staticEdgeStrings ) {
								if ( destinationState.canConsume( edgeString ) ) {
									//if a set exists that contains a string from another static
									//state, we are nondeterministic
									return false;
								}
							}
							++staticOrSetCount;
						}
						else {
							throw new AssertionError( "found non set edge in SetEdges: " + edge );
						}
					}
				}
			}

			{
				List<Edge> setEdges = this.setEdges.get( state.getIdx() );
				if ( setEdges.size() > 1 ) {
					for ( Edge fst : setEdges ) {
						State destinationStateFst = this.states.get( fst.destination );
						if(!destinationStateFst.isSet()) {
							throw new AssertionError( "found non set edge in SetEdges: " + fst );
						}
						SetState setStateFst = (SetState) destinationStateFst;

						for ( Edge snd : setEdges ) {
							if ( fst == snd ) {
								//they are the same object don't check that
								continue;
							}
							State destinationStateSnd = this.states.get( snd.destination );
							if(!destinationStateSnd.isSet()) {
								throw new AssertionError( "found non set edge in SetEdges: " + snd );
							}
							SetState setStateSnd = (SetState) destinationStateSnd;
							if(setStateFst.criterion.intersects( setStateSnd.criterion )) {
								return false;
							}
						}
					}
				}
			}
		}

		//Bound edges are only allowed
		//at the beginning or the end
		//
		//or we ignore these, as they don't harm anyone. other non-determinism is already
		//handled
		{
			//END-like boundaries
			//if ( state.isBound() ) {
			//	if ( edges.size() != 1 && edges.iterator().next() != Moa.SNK ) {
			//		return false;
			//	}
			//}

			//START-like boundaries
			//if ( boundState != null ) {
			//if ( state != Moa.SRC || staticOrSetCount > 0 ) {
			//	return false;
			//}
			//}
		}

		//make sure that if there is a VariableState (Binding/Reference)
		//there is no other destination than SNK
		if ( variableDestinationState != null ) {
			if ( staticOrSetCount >= 2 || boundState != null ) {
				return false;
			}
			if ( staticOrSetCount == 1 ) {
				if ( staticDestinationStates.values().iterator().next() != SNK ) {
					return false;
				}
			}
		}

		if ( boundState != null ) {
			if ( staticOrSetCount > 0 ) {
				return false;
			}
		}
		return true;
	}

	public boolean isDeterministic() {
		for ( State state : this.states.values() ) {
			if ( !this.checkDeterminism( state ) ) {
				return false;
			}
		}
		return true;
	}

	public int maximalNextTokenLength(CurStateHolder stateHolder, Map<String, Variable> vars) {
		int maxLen = -1;

		if ( this.boundEdges.get( stateHolder.getState().getIdx() ).size() > 0 ) {
			maxLen = 0;
		}

		{
			//we assume all of the outgoing edges to be of equal length for
			//the static edges (via construction these are always of length 1)
			Map<EfficientString, Edge> staticEdges = this.staticEdges.get( stateHolder.getState().getIdx() );
			if ( staticEdges != null ) {
				for ( Map.Entry<EfficientString, Edge> entry : staticEdges.entrySet() ) {
					Edge edge = entry.getValue();
					if ( edge.destination == Moa.SNK.getIdx() ) {
						//ignore the SNK, there might be a backreference edge as well
						//SNK gets handled together with this
						//ignoring this here doesn't change the asymptotic
						//runtime
						continue;
					}
					State state = this.states.get( edge.destination );
					maxLen = Math.max( maxLen, state.getEdgeString( vars ).codePointLength() );
					if ( maxLen > 0 ) {
						return maxLen;
					}
				}
			}
		}

		{
			List<Edge> setEdges = this.setEdges.get( stateHolder.getState().getIdx() );
			if ( setEdges != null ) {
				for ( Edge edge : setEdges ) {
					if ( edge.destination == Moa.SNK.getIdx() || edge.destination == Moa.SRC.getIdx() ) {
						//destination is never SRC or SNK :)
						throw new AssertionError();
					}
					State state = this.states.get( edge.destination );
					maxLen = Math.max( maxLen, ((SetState) state).length );
					if ( maxLen > 0 ) {
						return maxLen;
					}
				}
			}
		}

		if ( maxLen != -1 ) {
			return maxLen;
		}
		else {
			maxLen = 0;
		}

		//fallback to "bruteforce", but this is only needed
		//in case we find a backreference
		//in which case there are at most 2 edges (=> O(n))
		assert this.edges.get( stateHolder.getState().getIdx() ).size() <= 2;
		{
			for ( Edge edge : this.edges.get( stateHolder.getState().getIdx() ) ) {
				State otherEnd = this.states.get( edge.destination );
				maxLen = Math.max( maxLen, otherEnd.getEdgeString( vars ).codePointLength() );
			}
		}
		return maxLen;
	}

	public List<Edge> getEdges(State state) {
		if ( !this.edges.containsKey( state.getIdx() ) ) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList( this.edges.get( state.getIdx() ) );
	}

	public Collection<State> getStates() {
		return Collections.unmodifiableCollection( this.states.values() );
	}

	public State getState(int idx) {
		return this.states.get( idx );
	}

	public void addEdgesWithDeterminismCheck(State from, Collection<Edge> edges) {
		this.checkNotFrozen();
		for ( Edge edge : edges ) {
			this.addEdge( from, edge );
		}
		if ( !this.checkDeterminism( from ) ) {
			throw new NonDeterministicException( "inserting " + edges + "as edges edges for " + from + " would make this MOA non-deterministic" );
		}
	}

	public void addEdgeWithDeterminismCheck(State from, Edge edge, Object source) {
		this.addEdge( from, edge );
		if ( !this.checkDeterminism( from ) ) {
			throw new NonDeterministicException(
					"inserting an edge from " + from + " to " + this.states.get( edge.destination ) + " would make this MOA non-deterministic. This was caused by " + source
							.toString()
			);
		}
	}

	private void addEdge(State from, Edge edge) {
		this.checkNotFrozen();
		this.edges.get( from.getIdx() ).add( edge );
		{
			State state = this.states.get( edge.destination );
			Integer src = from.getIdx();
			if ( state.isStatic() ) {
				//only called for terminals so null is fine
				this.staticEdges.get( src ).put( state.getEdgeString( null ), edge );
			}
			else if ( state.isSet() ) {
				this.setEdges.get( src ).add( edge );
			}
			else if ( state.isBound() ) {
				this.boundEdges.get( src ).add( edge );
			}
			else {
				this.backRefOrEpsilonEdges.get( src ).add( edge );
			}
		}
	}

	public void addState(State state) {
		this.checkNotFrozen();
		if ( this.edges.containsKey( state.getIdx() ) ) {
			//ignore this
			return;
		}
		this.states.put( state.getIdx(), state );
		this.edges.put( state.getIdx(), new ArrayList<>() );
		this.staticEdges.computeIfAbsent( state.getIdx(), key -> new HashMap<>() );
		this.setEdges.computeIfAbsent( state.getIdx(), key -> new ArrayList<>() );
		this.boundEdges.computeIfAbsent( state.getIdx(), key -> new ArrayList<>() );
		this.backRefOrEpsilonEdges.computeIfAbsent(
				state.getIdx(),
				key -> new ArrayList<>()
		);
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		EdgeGraph edgeGraph = (EdgeGraph) o;

		if ( frozen != edgeGraph.frozen ) {
			return false;
		}
		if ( states != null ? !states.equals( edgeGraph.states ) : edgeGraph.states != null ) {
			return false;
		}
		if ( edges != null ? !edges.equals( edgeGraph.edges ) : edgeGraph.edges != null ) {
			return false;
		}
		if ( setEdges != null ? !setEdges.equals( edgeGraph.setEdges ) : edgeGraph.setEdges != null ) {
			return false;
		}
		if ( boundEdges != null ? !boundEdges.equals( edgeGraph.boundEdges ) : edgeGraph.boundEdges != null ) {
			return false;
		}
		return !(staticEdges != null ? !staticEdges.equals( edgeGraph.staticEdges ) : edgeGraph.staticEdges != null);

	}

}
