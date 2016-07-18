package com.github.s4ke.moar.moa;

import java.util.HashMap;
import java.util.Map;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.moa.edgegraph.CurStateHolder;
import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.MatchInfo;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
final class MoaMatcherImpl implements CurStateHolder, MoaMatcher {

	private final EdgeGraph edges;
	private final Map<String, Variable> vars;
	private final Map<Integer, Variable> varsByOccurence;
	private CharSequence str;

	private MatchInfo mi = new MatchInfo();
	private int lastStart = -1;
	private State state = Moa.SRC;

	MoaMatcherImpl(EdgeGraph edges, Map<String, Variable> vars, CharSequence str) {
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
		this.mi = new MatchInfo();
	}

	private void resetStateAndVars() {
		this.state = Moa.SRC;
		this.vars.values().forEach( Variable::reset );
	}

	@Override
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

	@Override
	public String replaceFirst(String replacement) {
		this.reset();
		if ( this.nextMatch() ) {
			int matchLength = this.mi.getPos() - this.lastStart;
			int retLength = this.str.length() - matchLength + replacement.length();
			StringBuilder ret = new StringBuilder( retLength );
			for ( int i = 0; i < this.lastStart; ++i ) {
				ret.append( this.str.charAt( i ) );
			}
			for ( int i = 0; i < replacement.length(); ++i ) {
				ret.append( replacement.charAt( i ) );
			}
			for ( int i = this.mi.getPos(); i < this.str.length(); ++i ) {
				ret.append( this.str.charAt( i ) );
			}
			return ret.toString();
		}
		else {
			return this.str.toString();
		}
	}

	@Override
	public String replaceAll(String replacement) {
		this.reset();

		StringBuilder ret = new StringBuilder( this.str.length() );
		int prefixStart = 0;
		while ( this.nextMatch() ) {
			for ( int i = prefixStart; i < this.lastStart; ++i ) {
				ret.append( this.str.charAt( i ) );
			}
			for ( int i = 0; i < replacement.length(); ++i ) {
				ret.append( replacement.charAt( i ) );
			}
			prefixStart = this.mi.getLastMatch();
		}
		for ( int i = this.mi.getLastMatch(); i < this.str.length(); ++i ) {
			ret.append( this.str.charAt( i ) );
		}
		return ret.toString();
	}

	@Override
	public boolean nextMatch() {
		return this.nextMatch( true );
	}

	private boolean nextMatch(boolean advanceOnReject) {
		this.resetStateAndVars();
		EdgeGraph.StepResult stepResult = null;
		int curStart = this.mi.getPos();
		this.mi.setWholeString( this.str );
		EfficientString token = new EfficientString();
		this.mi.setString( token );
		int strLen = this.str.length();

		while ( !this.isFinished() && this.mi.getPos() < strLen ) {
			curStart = this.mi.getPos();
			while ( !this.isFinished() && this.mi.getPos() < strLen ) {
				int tokenLen = this.edges.maximalNextTokenLength( this, this.vars );
				if ( this.mi.getPos() + tokenLen > strLen ) {
					if ( tokenLen > 1 ) {
						//reference
						this.mi.setPos( curStart );
					}
					this.mi.setPos( this.mi.getPos() + 1 );
					break;
				}
				token.update( this.str, this.mi.getPos(), this.mi.getPos() + tokenLen );
				{
					stepResult = this.step( mi );
					if ( stepResult == EdgeGraph.StepResult.REJECTED ) {
						if ( !advanceOnReject ) {
							return false;
						}
						if ( tokenLen > 1 ) {
							//reference
							this.mi.setPos( curStart );
						}
						if ( tokenLen > 0 ) {
							this.mi.setPos( this.mi.getPos() + 1 );
						}
						else {
							//this is in case we failed on a "epsilon"-style edge
							//like end of input
							//FIXME: can this be done better?
							this.mi.setPos( curStart + 1 );
						}
						break;
					}
					if ( stepResult == EdgeGraph.StepResult.CONSUMED ) {
						this.mi.setPos( this.mi.getPos() + tokenLen );
					}
				}
			}
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

		//allow to get into the final state for the case we can have an epsilon string (
		if ( !this.isFinished() && this.mi.getLastMatch() != this.mi.getPos() ) {
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

		if ( this.isFinished() ) {
			this.mi.setLastMatch( this.mi.getPos() );
		}

		return this.isFinished();
	}

	@Override
	public boolean matches() {
		this.reset();
		if ( this.nextMatch( false ) ) {
			if ( this.lastStart == 0 && this.mi.getPos() >= this.str.length() ) {
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
	@Override
	public String getVariableContent(int occurence) {
		Variable var = this.varsByOccurence.get( occurence );
		if ( var == null ) {
			throw new IllegalArgumentException( "variable with occurence " + occurence + " does not exist" );
		}
		return var.getContents();
	}

	@Override
	public String getVariableContent(String name) {
		Variable var = this.vars.get( name );
		if ( var == null ) {
			throw new IllegalArgumentException( "variable with name " + name + " does not exist" );
		}
		return var.getContents();
	}

}
