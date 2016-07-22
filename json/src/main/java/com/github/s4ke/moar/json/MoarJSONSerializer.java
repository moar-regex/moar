package com.github.s4ke.moar.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.ActionType;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.BasicState;
import com.github.s4ke.moar.moa.states.BoundState;
import com.github.s4ke.moar.moa.states.SetState;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.moa.states.VariableState;
import com.github.s4ke.moar.regex.BoundConstants;
import com.github.s4ke.moar.regex.CharacterClassesUtils;
import com.github.s4ke.moar.regex.parser.RegexLexer;
import com.github.s4ke.moar.regex.parser.RegexParser;
import com.github.s4ke.moar.regex.parser.RegexTreeListener;
import com.github.s4ke.moar.strings.EfficientString;
import com.github.s4ke.moar.util.Range;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Martin Braun
 */
public final class MoarJSONSerializer {

	private static final MoaPattern MEMORY_ACTION_PATTERN = MoaPattern.compile(
			"(?<action>[ocr])\\((?<name>[a-zA-Z0-9]+)\\)"
	);

	private MoarJSONSerializer() {
		//can't touch this!
	}

	public static String toJSON(MoaPattern moaPattern) {
		JSONObject[] json = new JSONObject[1];
		moaPattern.accessMoa(
				(moa) -> json[0] = toJSON( moa )
		);
		json[0].put( "regex", moaPattern.getRegex() );
		return json[0].toString();
	}

	public static MoaPattern fromJSON(String jsonString) {
		JSONObject jsonObject = new JSONObject( jsonString );
		Moa moa = new Moa();

		Map<String, Variable> variables = new HashMap<>();
		{
			JSONArray varsArray = jsonObject.optJSONArray( "vars" );
			if ( varsArray != null ) {
				for ( int i = 0; i < varsArray.length(); ++i ) {
					String variableName = varsArray.getString( i );
					variables.put( variableName, new Variable( variableName ) );
				}
			}
		}
		moa.setVariables( variables );

		EdgeGraph edgeGraph = new EdgeGraph();
		edgeGraph.addState( Moa.SRC );
		edgeGraph.addState( Moa.SNK );

		{
			JSONArray statesArray = jsonObject.getJSONArray( "states" );
			for ( int i = 0; i < statesArray.length(); ++i ) {
				JSONObject stateObj = statesArray.getJSONObject( i );
				int idx = stateObj.getInt( "idx" );
				State state;
				if ( stateObj.has( "name" ) ) {
					state = new BasicState( idx, stateObj.getString( "name" ) );
				}
				else if ( stateObj.has( "ref" ) ) {
					state = new VariableState( idx, stateObj.getString( "ref" ) );
				}
				else if ( stateObj.has( "bound" ) ) {
					String boundIdent = stateObj.getString( "bound" );
					state = new BoundState( idx, boundIdent, BoundConstants.getFN( boundIdent ) );
				}
				else if ( stateObj.has( "set" ) ) {
					//TODO: this is a bit hacky, maybe do this better?
					String setString = stateObj.getString( "set" );
					Function<EfficientString, Boolean> setDescriptor;
					if ( setString.startsWith( "[" ) ) {
						setDescriptor = buildFnFromSetExpression( setString );
					}
					else {
						setDescriptor = CharacterClassesUtils.getFn( setString );
					}
					state = new SetState( idx, 1, setDescriptor, stateObj.getString( "set" ) );
				}
				else {
					throw new IllegalArgumentException(
							"state must have either name, ref, bound or set in the JSON string"
					);
				}
				edgeGraph.addState( state );
			}
		}

		{
			JSONArray edgesArray = jsonObject.getJSONArray( "edges" );
			for ( int i = 0; i < edgesArray.length(); ++i ) {
				State fromState;
				State toState;
				Set<MemoryAction> memoryActionSet = new HashSet<>();
				{
					JSONObject edgeObj = edgesArray.getJSONObject( i );

					{
						int from = edgeObj.getInt( "from" );
						fromState = edgeGraph.getState( from );
						if ( fromState == null ) {
							throw new IllegalArgumentException( "a state with idx " + from + " does not exist" );
						}
						int to = edgeObj.getInt( "to" );
						toState = edgeGraph.getState( to );
						if ( toState == null ) {
							throw new IllegalArgumentException( "a state with idx " + to + " does not exist" );
						}
					}

					JSONArray memoryActions = edgeObj.optJSONArray( "memoryActions" );
					if ( memoryActions != null ) {
						for ( int j = 0; j < memoryActions.length(); ++j ) {
							String memoryActionString = memoryActions.getString( j );
							MoaMatcher matcher = MEMORY_ACTION_PATTERN.matcher( memoryActionString );
							if ( matcher.matches() ) {
								String action = matcher.getVariableContent( "action" );
								String variableName = matcher.getVariableContent( "name" );
								memoryActionSet.add(
										new MemoryAction(
												ActionType.fromString( action ),
												variableName
										)
								);
							}
						}
					}
				}

				edgeGraph.addEdge( fromState, new EdgeGraph.Edge( memoryActionSet, toState ) );
			}
		}

		moa.setEdges( edgeGraph );
		moa.freeze();

		String regexString = jsonObject.optString( "regex" );
		return MoaPattern.build( moa, regexString );
	}

	private static JSONObject toJSON(Moa moa) {
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
					if ( edge.memoryAction.size() > 0 ) {
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
		return obj;
	}

	private static Function<EfficientString, Boolean> buildFnFromSetExpression(String setExpression) {
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( setExpression ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		parser.getErrorListeners().clear();
		parser.addErrorListener(
				new BaseErrorListener() {
					@Override
					public void syntaxError(
							Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e) {
						throw e;
					}
				}
		);
		RegexParser.SetContext setContext = parser.set();
		if ( parser.getNumberOfSyntaxErrors() > 0 ) {
			throw new IllegalArgumentException( "malformed set expression: " + setExpression );
		}
		Range[] ranges;
		boolean negative;
		if ( setContext.negativeSet() != null ) {
			ranges = RegexTreeListener.ranges( setContext.negativeSet().setItems() );
			negative = true;
		}
		else if ( setContext.positiveSet() != null ) {
			ranges = RegexTreeListener.ranges( setContext.positiveSet().setItems() );
			negative = false;
		}
		else {
			throw new AssertionError();
		}
		if ( negative ) {
			return CharacterClassesUtils.negativeFn( ranges );
		}
		else {
			return CharacterClassesUtils.positiveFn( ranges );
		}
	}

}
