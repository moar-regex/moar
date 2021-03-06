/*
 The MIT License (MIT)

 Copyright (c) 2016 Martin Braun

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
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
import com.github.s4ke.moar.util.CharSeq;
import com.github.s4ke.moar.util.IntCharSeq;

/**
 * @author Martin Braun
 */
final class MoaMatcherImpl implements CurStateHolder, MoaMatcher {

	private final EdgeGraph edges;
	private final Map<String, Variable> vars;
	private final Map<Integer, Variable> varsByOccurence;
	private CharSeq str;

	private MatchInfo mi = new MatchInfo();
	private int lastStart = -1;
	private State state = Moa.SRC;

	MoaMatcherImpl(EdgeGraph edges, Map<String, Variable> vars, CharSeq str) {
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
	public MoaMatcher reuse(CharSequence str) {
		this.reset();
		this.str = new IntCharSeq( str );
		return this;
	}

	@Override
	public MoaMatcher reuse(CharSeq seq) {
		this.reset();
		this.str = seq;
		return this;
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
			int retLength = this.str.codePointLength() - matchLength + replacement.length();
			StringBuilder ret = new StringBuilder( retLength );
			for ( int i = 0; i < this.lastStart; ++i ) {
				ret.appendCodePoint( this.str.codePoint( i ) );
			}
			ret.append( replacement );
			for ( int i = this.mi.getPos(); i < this.str.codePointLength(); ++i ) {
				ret.appendCodePoint( this.str.codePoint( i ) );
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

		StringBuilder ret = new StringBuilder( this.str.codePointLength() );
		int prefixStart = 0;
		while ( this.nextMatch() ) {
			for ( int i = prefixStart; i < this.lastStart; ++i ) {
				ret.appendCodePoint( this.str.codePoint( i ) );
			}
			ret.append( replacement );
			prefixStart = this.mi.getLastMatch();
		}
		for ( int i = this.mi.getLastMatch(); i < this.str.codePointLength(); ++i ) {
			ret.appendCodePoint( this.str.codePoint( i ) );
		}
		return ret.toString();
	}

	@Override
	public int getStart() {
		if ( !this.isFinished() ) {
			throw new IllegalStateException( "did not match on the last call" );
		}
		return this.lastStart;
	}

	@Override
	public int getEnd() {
		if ( !this.isFinished() ) {
			throw new IllegalStateException( "did not match on the last call" );
		}
		return this.mi.getLastMatch();
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

		int strLen = this.str.codePointLength();

		while ( !this.isFinished() && this.mi.getPos() < strLen ) {
			curStart = this.mi.getPos();
			while ( !this.isFinished() && this.mi.getPos() < strLen ) {
				int tokenLen = this.edges.maximalNextTokenLength( this, this.vars );
				if ( this.mi.getPos() + tokenLen > strLen ) {
					if ( tokenLen > 0 ) {
						//we were rejected, but let's check if we have to
						//read \epsilon on the end instead
						token.reset();
						while ( !this.isFinished() && this.step( mi ) != EdgeGraph.StepResult.REJECTED ) {

						}
						if ( this.isFinished() ) {
							this.lastStart = curStart;
							this.mi.setLastMatch( this.mi.getPos() );
							return true;
						}
					}
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
						if ( tokenLen > 0 ) {
							//we were rejected, but let's check if we have to
							//read \epsilon on the end instead
							token.reset();
							while ( !this.isFinished() && this.step( mi ) != EdgeGraph.StepResult.REJECTED ) {

							}
							if ( this.isFinished() ) {
								this.lastStart = curStart;
								this.mi.setLastMatch( this.mi.getPos() );
								return true;
							}
						}
						this.mi.setPos( curStart + 1 );
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
			if ( this.lastStart == 0 && this.mi.getPos() >= this.str.codePointLength() ) {
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
