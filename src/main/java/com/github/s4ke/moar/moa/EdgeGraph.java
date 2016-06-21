package com.github.s4ke.moar.moa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.s4ke.moar.util.SubString;

/**
 * @author Martin Braun
 */
public class EdgeGraph {

	public static final State SRC = new BasicState( 0, "SRC" );
	//SNK's name must be equal to "" so that epsilon works
	public static final State SNK = new BasicState( 1, "" );

	private final Map<Integer, State> states = new HashMap<>();
	private final Map<Integer, Set<Edge>> edges = new HashMap<>();

	private State curState;

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
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

		public Edge(Set<MemoryAction> memoryAction, Integer destination) {
			this.memoryAction = memoryAction;
			this.destination = destination;
		}

		public final Set<MemoryAction> memoryAction;
		public final Integer destination;

		@Override
		public String toString() {
			return "Edge{" +
					"memoryAction=" + memoryAction +
					", destination=" + destination +
					'}';
		}
	}

	public void setState(State state) {
		this.curState = state;
	}

	public Edge getEdge(State from, String edgeString) {
		Set<Edge> set = this.edges.get( from.getIdx() );
		if ( set == null ) {
			return null;
		}
		Set<Edge> viable = set.stream().filter(
				edge -> {
					State state = this.states.get( edge.destination );
					String otherEdgeString = state.getEdgeString();
					return otherEdgeString != null && otherEdgeString.equals(
							edgeString
					);
				}
		).collect(
				Collectors.toSet()
		);
		if ( viable.size() > 1 ) {
			return null;
		}
		if ( viable.size() == 0 ) {
			return null;
		}
		return viable.iterator().next();
	}

	public enum StepResult {
		CONSUMED,
		NOT_CONSUMED,
		REJECTED
	}

	public State getCurState() {
		return this.curState;
	}

	public StepResult step(SubString ch, Map<String, Variable> vars) {
		{
			EdgeGraph.Edge edge = this.getEdge( this.curState, ch.toString() );
			if ( edge != null ) {
				State destinationState = this.states.get( edge.destination );
				edge.memoryAction.forEach( memoryAction -> memoryAction.act( vars ) );
				//we have found an edge so we can accept this input
				for ( Variable var : vars.values() ) {
					if ( var.canConsume() ) {
						var.consume( ch );
					}
				}
				this.curState = destinationState;
				this.curState.touch();
				return StepResult.CONSUMED;
			}
		}

		/*
		{
			Edge epsilonEdge = this.getEdge( this.curState, "" );
			if ( epsilonEdge != null ) {
				State destinationState = this.states.get( epsilonEdge.destination );
				epsilonEdge.memoryAction.forEach( memoryAction -> memoryAction.act( vars ) );
				this.curState = destinationState;
				this.curState.touch();
				return StepResult.NOT_CONSUMED;
			}
		}*/

		return StepResult.REJECTED;
	}

	public int maximalNextTokenLength() {
		int maxLen = 0;
		{
			for ( Edge edge : this.edges.get( curState.getIdx() ) ) {
				State otherEnd = this.states.get( edge.destination );
				maxLen = Math.max(maxLen, otherEnd.getEdgeString().length());
			}
		}
		return maxLen;
	}

	public Set<Edge> getEdges(State state) {
		if ( !this.edges.containsKey( state.getIdx() ) ) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet( this.edges.get( state.getIdx() ) );
	}

	public Collection<State> getStates() {
		return Collections.unmodifiableCollection( this.states.values() );
	}

	public void addEdge(State from, Edge edge) {
		this.checkNotFrozen();
		this.edges.get( from.getIdx() ).add( edge );
	}

	public void addState(State state) {
		this.checkNotFrozen();
		if ( this.edges.containsKey( state.getIdx() ) ) {
			//ignore this
			return;
		}
		this.states.put( state.getIdx(), state );
		this.edges.put( state.getIdx(), new HashSet<>() );
	}

}
