package com.github.s4ke.moar.moa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.NotDeterministicException;
import com.github.s4ke.moar.util.SubString;


/**
 * @author Martin Braun
 */
public class Moa {

	public static final State SRC = EdgeGraph.SRC;
	public static final State SNK = EdgeGraph.SNK;

	private Map<String, Variable> vars = new HashMap<>();
	private Map<Integer, Variable> varsByOccurence = new HashMap<>();
	private Map<String, List<VariableState>> varStates = new HashMap<>();

	private EdgeGraph edges = new EdgeGraph();

	public Moa() {
		this.reset();
	}

	private boolean frozen = false;

	public void freeze() {
		this.frozen = true;
		this.edges.freeze();
		this.varStates.clear();
		this.getStates().stream().filter( state -> state instanceof VariableState ).forEach(
				state -> {
					VariableState varState = (VariableState) state;
					varStates.computeIfAbsent( varState.getVariable().name, (key) -> new ArrayList<>() ).add(
							varState
					);
				}
		);
		this.varStates.values().forEach(
				(list) -> Collections.sort(
						list, (a, b) -> Integer.compare(
								a.getIdx(),
								b.getIdx()
						)
				)
		);
		if ( !this.edges.isDeterministic() ) {
			throw new NotDeterministicException( "this moa is not deterministic" );
		}
	}

	public boolean isDeterministic() {
		return this.edges.isDeterministic();
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

	public void reset() {
		this.edges.setState( SRC );
		for ( Variable var : this.vars.values() ) {
			var.reset();
			if ( this.varStates.containsKey( var.name ) ) {
				this.varStates.get( var.name ).forEach( VariableState::reset );
			}
		}
	}

	private EdgeGraph.StepResult step(SubString ch) {
		return this.edges.step( ch, this.vars );
	}

	public boolean isFinished() {
		return this.edges.getCurState() == SNK;
	}

	public void setEdges(EdgeGraph edges) {
		this.checkNotFrozen();
		this.edges = edges;
	}

	public Collection<State> getStates() {
		return this.edges.getStates();
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
					variablesHandled.add( ma.variable );
				}
			}
		}
		return ret;
	}

	public void setVariables(Map<String, Variable> variables) {
		this.checkNotFrozen();
		this.vars = variables;
		for ( Variable var : this.vars.values() ) {
			this.varsByOccurence.put( var.getOccurenceInRegex(), var );
		}
	}

	public boolean check(String str) {
		this.checkFrozen();
		this.reset();
		int pos = 0;
		SubString token = new SubString();
		int strLen = str.length();
		while ( pos < strLen ) {
			int tokenLen = this.edges.maximalNextTokenLength();
			if ( pos + tokenLen > strLen ) {
				return false;
			}
			token.update( str, pos, pos + tokenLen );
			if ( this.step( token ) == EdgeGraph.StepResult.REJECTED ) {
				return false;
			}
			pos += tokenLen;
		}
		token.reset();
		//noinspection StatementWithEmptyBody
		while ( this.step( token ) == EdgeGraph.StepResult.CONSUMED ) {
		}
		return this.isFinished();
	}

	/**
	 * @param occurence 1-based
	 */
	public String getVariableContent(int occurence) {
		Variable var = this.varsByOccurence.get( occurence );
		if ( var == null ) {
			throw new IllegalArgumentException( "variable with occurence " + occurence + " does not exist" );
		}
		return var.getContents();
	}

	public String getVariableContent(String variableName, int occurence) {
		if ( !this.varStates.containsKey( variableName ) ) {
			throw new IllegalArgumentException( "Variable " + variableName + " does not exist" );
		}
		VariableState varState = this.varStates.get( variableName ).get( occurence );
		if ( varState == null ) {
			throw new IllegalArgumentException( "Variable " + variableName + " does not have a " + occurence + " occurence" );
		}
		return varState.getContents();
	}

}
