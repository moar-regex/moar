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
	private final Map<Integer, Map<String, Edge>> staticEdges = new HashMap<>();

	private State curState;

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
		for ( Map.Entry<Integer, Set<Edge>> entry : this.edges.entrySet() ) {
			Integer src = entry.getKey();
			Map<String, Edge> map = new HashMap<>();
			this.staticEdges.put( src, map );
			entry.getValue().forEach(
					(edge) -> {
						State state = this.states.get( edge.destination );
						if ( !(state instanceof VariableState) ) {
							map.put( state.getEdgeString(), edge );
						}
					}
			);
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

		{
			Map<String, Edge> staticEdges = this.staticEdges.get( from.getIdx() );
			if ( staticEdges != null ) {
				Edge edge = staticEdges.get( edgeString );
				if ( edge != null ) {
					return edge;
				}
			}
		}

		//this is only reached for the case that we
		//are in a state with a backreference
		//in this case we have at most 2 edges
		//so this doesn't change the overall runtime
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
			throw new IllegalStateException( "non-determinism detected, multiple edges for string: " + edgeString + ". The edges were: " + set );
		}
		if ( viable.size() == 0 ) {
			return null;
		}
		return viable.iterator().next();
	}

	public enum StepResult {
		CONSUMED,
		REJECTED
	}

	public State getCurState() {
		return this.curState;
	}

	public StepResult step(SubString ch, Map<String, Variable> vars) {
		{
			//TODO: can we avoid the toString call here somehow?
			//maybe move it to the getEdge method as
			//for variable Strings we can have a more specialized
			//version of comparison that works as well
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
		return StepResult.REJECTED;
	}

	public boolean isDeterministic() {
		return true;
	}

	public int maximalNextTokenLength() {
		int maxLen = -1;

		//we assume all of the outgoing edges to be of equal length for
		//the static edges (via construction these are always of length 1)
		Map<String, Edge> staticEdges = this.staticEdges.get( this.curState.getIdx() );
		if ( staticEdges != null ) {
			for ( Map.Entry<String, Edge> entry : staticEdges.entrySet() ) {
				Edge edge = entry.getValue();
				if ( edge.destination == Moa.SNK.getIdx() ) {
					//ignore the SNK, there might be a backreference edge as well
					//SNK gets handled together with this
					//ignoring this here doesn't change the asymptotic
					//runtime
					continue;
				}
				State state = this.states.get( edge.destination );
				maxLen = Math.max( maxLen, state.getEdgeString().length() );
				if ( maxLen > 0 ) {
					return maxLen;
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
		{
			for ( Edge edge : this.edges.get( curState.getIdx() ) ) {
				State otherEnd = this.states.get( edge.destination );
				maxLen = Math.max( maxLen, otherEnd.getEdgeString().length() );
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
