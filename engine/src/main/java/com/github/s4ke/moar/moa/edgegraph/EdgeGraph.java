package com.github.s4ke.moar.moa.edgegraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
 * @author Martin Braun
 */
public final class EdgeGraph {

	public static final State SRC = new BasicState( 0, "SRC" );
	//SNK's name must be equal to "" so that epsilon works
	public static final State SNK = new BasicState( 1, "" );

	private final Map<Integer, State> states = new HashMap<>();
	private final Map<Integer, Set<Edge>> edges = new HashMap<>();
	private final Map<Integer, Set<Edge>> setEdges = new HashMap<>();
	private final Map<Integer, Set<Edge>> boundEdges = new HashMap<>();
	private final Map<Integer, Map<EfficientString, Edge>> staticEdges = new HashMap<>();

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
		for ( Map.Entry<Integer, Set<Edge>> entry : this.edges.entrySet() ) {
			Integer src = entry.getKey();
			Map<EfficientString, Edge> stat = new HashMap<>();
			this.staticEdges.put( src, stat );
			Set<Edge> setEdges = new HashSet<>();
			this.setEdges.put( src, setEdges );
			Set<Edge> boundEdges = new HashSet<>();
			this.boundEdges.put( src, boundEdges );
			entry.getValue().forEach(
					(edge) -> {
						State state = this.states.get( edge.destination );
						if ( state.isStatic() ) {
							//only called for terminals so null is fine
							stat.put( state.getEdgeString( null ), edge );
						}
						else if ( state.isSet() ) {
							setEdges.add( edge );
						}
						else if ( state.isBound() ) {
							boundEdges.add( edge );
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
			Set<Edge> setEdges = this.setEdges.get( from.getIdx() );
			if ( setEdges != null ) {
				for ( Edge edge : setEdges ) {
					State state = this.states.get( edge.destination );
					if ( state.isSet() && state.canConsume( matchInfo.getString() ) ) {
						return edge;
					}
				}
			}
		}

		Set<Edge> set = this.edges.get( from.getIdx() );
		if ( set == null ) {
			return null;
		}

		//this is only reached for the case that we
		//are in a state with a backreference
		//in this case we have at most 2 edges (the backref or possibly epsilon)
		//so this doesn't change the overall runtime
		Edge edgeRes = null;
		for ( Edge edge : set ) {
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

	public boolean isDeterministic() {
		for ( State state : this.states.values() ) {
			Set<Edge> edges = this.edges.get( state.getIdx() );
			int edgeCnt = edges.size();

			VariableState variableDestinationState = null;
			BoundState boundState = null;
			Map<String, State> staticDestinationStates = new HashMap<>( edgeCnt );
			int staticOrSetCount;

			{
				Set<String> staticEdgeStrings = new HashSet<>();
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
						String edgeString = destinationState.getEdgeString( null ).toString();
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
					Set<Edge> setEdges = this.setEdges.get( state.getIdx() );
					if ( setEdges != null ) {
						for ( Edge edge : setEdges ) {
							State destinationState = this.states.get( edge.destination );
							if ( destinationState.isSet() ) {
								for ( String edgeString : staticEdgeStrings ) {
									if ( destinationState.canConsume( new EfficientString( edgeString ) ) ) {
										//if a set exists that contains a string from another static
										//state, we are nondeterministic
										return false;
									}
								}
								++staticOrSetCount;
							}
						}
					}
				}

				//check for overlapping SetStates
				//FIXME: can we do this more efficiently?
				{
					Set<Edge> setEdges = this.setEdges.get( state.getIdx() );
					if ( setEdges != null && setEdges.size() > 1 ) {
						for ( int i = 0; i <= Character.MAX_VALUE; ++i ) {
							char ch = (char) i;
							boolean foundOne = false;
							for ( Edge edge : edges ) {
								State destinationState = this.states.get( edge.destination );
								if ( destinationState.isSet() ) {
									if ( destinationState.canConsume( new EfficientString( String.valueOf( ch ) ) ) ) {
										//if a set exists that contains a string from another set
										//state, we are nondeterministic
										if ( foundOne ) {
											return false;
										}
										else {
											foundOne = true;
										}
									}
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
					maxLen = Math.max( maxLen, state.getEdgeString( vars ).length() );
					if ( maxLen > 0 ) {
						return maxLen;
					}
				}
			}
		}

		{
			Set<Edge> setEdges = this.setEdges.get( stateHolder.getState().getIdx() );
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
		{
			for ( Edge edge : this.edges.get( stateHolder.getState().getIdx() ) ) {
				State otherEnd = this.states.get( edge.destination );
				maxLen = Math.max( maxLen, otherEnd.getEdgeString( vars ).length() );
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