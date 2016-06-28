package com.github.s4ke.moar.moa;

import java.util.HashMap;
import java.util.Map;

import com.github.s4ke.moar.moa.edgegraph.CurStateHolder;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public final class Matcher implements CurStateHolder {

	private final EdgeGraph edges;
	private final Map<String, Variable> vars;
	private final Map<Integer, Variable> varsByOccurence;
	private CharSequence str;
	private State state = Moa.SRC;

	Matcher(EdgeGraph edges, Map<String, Variable> vars, CharSequence str) {
		this.edges = edges;
		this.vars = vars;
		this.str = str;
		this.varsByOccurence = new HashMap<>();
		for ( Variable var : vars.values() ) {
			this.varsByOccurence.put( var.getOccurenceInRegex(), var );
		}
	}

	public void reset() {
		this.state = Moa.SRC;
	}

	public void reuse(String str) {
		this.reset();
		this.str = str;
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	public boolean check() {
		this.reset();
		int pos = 0;
		EfficientString token = new EfficientString();
		int strLen = this.str.length();
		while ( pos < strLen ) {
			int tokenLen = this.edges.maximalNextTokenLength( this, this.vars );
			if ( pos + tokenLen > strLen ) {
				return false;
			}
			token.update( this.str, pos, pos + tokenLen );
			if ( this.step( token ) == EdgeGraph.StepResult.REJECTED ) {
				return false;
			}
			pos += tokenLen;
		}
		token.reset();
		//noinspection StatementWithEmptyBody
		while ( !this.isFinished() && this.step( token ) == EdgeGraph.StepResult.CONSUMED ) {
		}
		return this.isFinished();
	}

	private boolean isFinished() {
		return this.state == Moa.SNK;
	}

	private EdgeGraph.StepResult step(EfficientString ch) {
		return this.edges.step( this, ch, this.vars );
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

}
