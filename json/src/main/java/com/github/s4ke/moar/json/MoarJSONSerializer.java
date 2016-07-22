package com.github.s4ke.moar.json;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.BasicState;
import com.github.s4ke.moar.moa.states.BoundState;
import com.github.s4ke.moar.moa.states.SetState;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.moa.states.VariableState;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Martin Braun
 */
public final class MoarJSONSerializer {

	private MoarJSONSerializer() {
		//can't touch this!
	}

	public static String toJSON(MoaPattern moaPattern) {
		String[] json = new String[1];
		moaPattern.accessMoa(
				(moa) -> json[0] = toJSON( moa )
		);
		return json[0];
	}

	private static String toJSON(Moa moa) {
		JSONObject obj = new JSONObject();
		{
			JSONArray varArray = new JSONArray();
			for ( Variable var : moa.getVars().values() ) {
				String variableName = var.getName();
				varArray.put( variableName );
			}
			obj.put( "vars", varArray );
		}

		{
			JSONArray stateArray = new JSONArray();
			EdgeGraph edgeGraph = moa.getEdges();
			for ( State state : edgeGraph.getStates() ) {
				if ( state == Moa.SRC || state == Moa.SNK ) {
					// these are implicit
					continue;
				}
				JSONObject stateObj = new JSONObject();
				stateObj.put( "idx", state.getIdx() );
				if ( state.isStatic() ) {
					stateObj.put( "name", ((BasicState) state).getToken().toString() );
				}
				else if ( state.isVariable() ) {
					stateObj.put( "ref", ((VariableState) state).getVariableName() );
				}
				else if ( state.isBound() ) {
					stateObj.put( "bound", ((BoundState) state).getBoundHandled() );
				}
				else if ( state.isSet() ) {
					stateObj.put( "set", ((SetState) state).getStringRepresentation() );
				}
				stateArray.put( stateObj );
			}
			obj.put( "states", stateArray );
		}

		{
			JSONArray edgesArray = new JSONArray();
			EdgeGraph edgeGraph = moa.getEdges();
			for ( State state : edgeGraph.getStates() ) {
				for ( EdgeGraph.Edge edge : edgeGraph.getEdges( state ) ) {
					JSONObject edgeObject = new JSONObject();
					edgeObject.put( "from", state.getIdx() );
					edgeObject.put( "to", edge.destination );
					{
						JSONArray memoryActions = new JSONArray();
						for ( MemoryAction memoryAction : edge.memoryAction ) {
							memoryActions.put( memoryAction.actionType.toString( memoryAction.variable ) );
						}
						edgeObject.put( "memoryActions", memoryActions );
					}
					edgesArray.put( edgeObject );
				}
			}
			obj.put( "edges", edgesArray );
		}
		return obj.toString();
	}

}
