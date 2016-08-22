package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * Basic implementation of {@link State} that represents a
 * a String as a token. Currently this is expected to be a String
 * of length 1.
 *
 * @author Martin Braun
 */
public class BasicState implements State {

	public final int idx;
	public final EfficientString string;

	public BasicState(int idx, String string) {
		this.idx = idx;
		this.string = new EfficientString( string );
	}

	public EfficientString getToken() {
		return this.string;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public EfficientString getEdgeString(Map<String, Variable> variables) {
		return this.string;
	}

	@Override
	public boolean canConsume(EfficientString string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canConsume(MatchInfo matchInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isSet() {
		return false;
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public boolean isBound() {
		return false;
	}

	@Override
	public void touch() {

	}

	@Override
	public String toString() {
		return "BasicState{" +
				"idx=" + idx +
				", string='" + string + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		BasicState that = (BasicState) o;

		if ( idx != that.idx ) {
			return false;
		}
		return !(string != null ? !string.equals( that.string ) : that.string != null);

	}

}
