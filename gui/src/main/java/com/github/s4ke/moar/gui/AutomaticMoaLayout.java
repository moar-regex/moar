package com.github.s4ke.moar.gui;


import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.edgegraph.MemoryAction;
import com.github.s4ke.moar.moa.states.BasicState;
import com.github.s4ke.moar.moa.states.BoundState;
import com.github.s4ke.moar.moa.states.SetState;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.VariableState;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;

@SuppressWarnings("serial")
public class AutomaticMoaLayout extends JFrame {

	private static final String EXACT_CHAR = "\"";

	private final mxGraph graph;
	private final mxGraphComponent graphComponent;
	private final mxKeyboardHandler keyBoardHandler;

	private final Map<String, String> states = new HashMap<>();
	private final Map<String, EdgeRep> edges = new HashMap<>();

	private static class EdgeRep {
		private final String source;
		private final String dest;
		private final String id;

		private EdgeRep(String source, String dest, String id) {
			this.source = source;
			this.dest = dest;
			this.id = id;
		}
	}


	public static final class MoaGraph extends mxGraph {

	}

	public AutomaticMoaLayout() {
		super( "Moar-Visual" );
		graph = new MoaGraph();

		graph.getModel().beginUpdate();
		try {
			MoaPattern pattern = MoaPattern.compile( "^(?<toast>[a-z]b[^b]\\w)\\k<toast>$" );
			pattern.accessMoa(
					(moa) -> {
						EdgeGraph edgeGraph = moa.getEdges();
						Map<Integer, Object> stateVertices = new HashMap<>();

						int id = 0;
						{
							for ( State state : edgeGraph.getStates() ) {
								String strId = String.valueOf( id++ );
								Object vertex = graph.insertVertex(
										graph.getDefaultParent(),
										strId,
										toRepresentationString( state ),
										0,
										0,
										40,
										40
								);
								stateVertices.put( state.getIdx(), vertex );
							}
						}
						{
							for ( State state : edgeGraph.getStates() ) {
								for ( EdgeGraph.Edge edge : edgeGraph.getEdges( state ) ) {
									String strId = String.valueOf( id++ );
									graph.insertEdge(
											graph.getDefaultParent(),
											strId,
											getDescriptionString( edge.memoryAction ),
											stateVertices.get( state.getIdx() ),
											stateVertices.get( edge.destination )
									);
								}
							}
						}
					}
			);
		}
		finally {
			graph.getModel().endUpdate();
		}
		graphComponent = new mxGraphComponent( graph );

		keyBoardHandler = new mxKeyboardHandler( graphComponent );

		graph.addListener(
				mxEvent.REMOVE_CELLS, (sender, evt) -> {
					for ( Object object : (Object[]) evt.getProperties().get( "cells" ) ) {
						if ( graph.getModel().isEdge( object ) ) {

							System.out.println( object );
						}
					}
				}
		);

		getContentPane().add( graphComponent );
		layoutGraph();
	}

	private void layoutGraph() {
		mxGraphLayout layout = new mxOrganicLayout( graph );
		Object cell = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			layout.execute( cell );
		}
		finally {
			graph.getModel().endUpdate();
		}
	}

	public static String toRepresentationString(State state) {
		if ( state == Moa.SRC ) {
			return "SRC";
		}
		else if ( state == Moa.SNK ) {
			return "SNK";
		}
		return getDescriptionString( state );
	}

	public static String getDescriptionString(State state) {
		String str;
		if ( state.isStatic() ) {
			str = EXACT_CHAR + ((BasicState) state).getToken().toString() + EXACT_CHAR;
		}
		else if ( state.isVariable() ) {
			str = "&" + ((VariableState) state).getVariableName();
		}
		else if ( state.isBound() ) {
			str = ((BoundState) state).getBoundHandled();
		}
		else if ( state.isSet() ) {
			str = ((SetState) state).getStringRepresentation();
		}
		else {
			throw new AssertionError( "unrecognized State class " + state.getClass() );
		}
		return str;
	}

	public static String getDescriptionString(Set<MemoryAction> memoryActionSet) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for ( MemoryAction ma : memoryActionSet ) {
			if ( first ) {
				first = false;
			}
			else {
				str.append( ", " );
			}
			str.append( ma.actionType.toString( ma.variable ) );
		}
		return str.toString();
	}

	public static void main(String[] args) {
		AutomaticMoaLayout frame = new AutomaticMoaLayout();
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setSize( 1600, 900 );
		frame.setVisible( true );
	}

}