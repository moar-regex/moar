package com.github.s4ke.moar.moa;

import java.util.HashMap;
import java.util.Map;

import com.github.s4ke.moar.moa.edgegraph.CurStateHolder;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.MatchInfo;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public final class MoaMatcher implements CurStateHolder {

	private final EdgeGraph edges;
	private final Map<String, Variable> vars;
	private final Map<Integer, Variable> varsByOccurence;
	private CharSequence str;
	private int pos = 0;
	private int lastStart = -1;
	private State state = Moa.SRC;

	MoaMatcher(EdgeGraph edges, Map<String, Variable> vars, CharSequence str) {
		this.edges = edges;
		this.vars = vars;
		this.str = str;
		this.varsByOccurence = new HashMap<>();
		for ( Variable var : vars.values() ) {
			this.varsByOccurence.put( var.getOccurenceInRegex(), var );
		}
	}

	public void reset() {
		this.resetStateAndVars();
		this.pos = 0;
	}

	private void resetStateAndVars() {
		this.state = Moa.SRC;
		this.vars.values().forEach( Variable::reset );
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

	//FIXME: allow adding capturing groups to the replacement

	public String replaceFirst(String replacement) {
		this.reset();
		if ( this.nextMatch() ) {
			int matchLength = this.pos - this.lastStart;
			int retLength = this.str.length() - matchLength + replacement.length();
			StringBuilder ret = new StringBuilder( retLength );
			for ( int i = 0; i < this.lastStart; ++i ) {
				ret.append( this.str.charAt( i ) );
			}
			for ( int i = 0; i < replacement.length(); ++i ) {
				ret.append( replacement.charAt( i ) );
			}
			for ( int i = this.pos; i < this.str.length(); ++i ) {
				ret.append( this.str.charAt( i ) );
			}
			return ret.toString();
		}
		else {
			return this.str.toString();
		}
	}

	public boolean nextMatch() {
		this.resetStateAndVars();
		EdgeGraph.StepResult stepResult = null;
		int curStart = this.pos;
		MatchInfo mi = new MatchInfo();
		mi.setWholeString( this.str );
		EfficientString token = new EfficientString();
		mi.setString( token );
		int strLen = this.str.length();


		while ( !this.isFinished() && this.pos < strLen ) {
			curStart = this.pos;
			while ( !this.isFinished() && this.pos < strLen ) {
				int tokenLen = this.edges.maximalNextTokenLength( this, this.vars );
				if ( this.pos + tokenLen > strLen ) {
					if ( tokenLen > 1 ) {
						//reference
						this.pos = curStart;
					}
					++this.pos;
					break;
				}
				mi.setPos( this.pos );
				token.update( this.str, this.pos, this.pos + tokenLen );
				{
					stepResult = this.step( mi );
					if ( stepResult == EdgeGraph.StepResult.REJECTED ) {
						if ( tokenLen > 1 ) {
							//reference
							this.pos = curStart;
						}
						++this.pos;
						break;
					}
					if ( stepResult == EdgeGraph.StepResult.CONSUMED ) {
						this.pos += tokenLen;
					}
				}
			}
			mi.setPos( this.pos );
			token.reset();
			//noinspection StatementWithEmptyBody
			while ( stepResult != EdgeGraph.StepResult.REJECTED && !this.isFinished() && this.step( mi ) != EdgeGraph.StepResult.REJECTED ) {

			}

			if ( !this.isFinished() ) {
				this.resetStateAndVars();
			}
			else {
				this.lastStart = curStart;
			}
		}

		{
			mi.setPos( this.pos );
			token.reset();
			//noinspection StatementWithEmptyBody
			while ( stepResult != EdgeGraph.StepResult.REJECTED && !this.isFinished() && this.step( mi ) != EdgeGraph.StepResult.REJECTED ) {

			}

			if ( !this.isFinished() ) {
				this.resetStateAndVars();
			}
			else {
				this.lastStart = curStart;
			}
		}

		return this.isFinished();
	}

	public boolean checkAsSingleWord() {
		this.reset();
		if ( this.nextMatch() ) {
			if ( this.lastStart == 0 && this.pos >= this.str.length() ) {
				return true;
			}
		}
		return false;
	}

	private boolean isFinished() {
		return this.state == Moa.SNK;
	}

	private EdgeGraph.StepResult step(MatchInfo mi) {
		return this.edges.step( this, mi, this.vars );
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

	public String getVariableContent(String name) {
		Variable var = this.vars.get( name );
		if ( var == null ) {
			throw new IllegalArgumentException( "variable with name " + name + " does not exist" );
		}
		return var.getContents();
	}

}
