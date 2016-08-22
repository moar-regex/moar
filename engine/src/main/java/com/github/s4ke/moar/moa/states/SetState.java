package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.CodePointSet;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * Implementation of a {@link State} that represents a whole range of characters. These
 * are represented by a {@link CodePointSet}. With this we don't have to create that
 * many states if we just want to allow every character that is part of a set (like [a-z], 1 state instead of 26)
 *
 * @author Martin Braun
 */
public class SetState implements State {

	public final int idx;
	public final int length;
	public final CodePointSet criterion;
	public final String stringRepresentation;

	public SetState(int idx, int length, CodePointSet criterion, String stringRepresentation) {
		this.idx = idx;
		this.length = length;
		this.criterion = criterion;
		this.stringRepresentation = stringRepresentation;
	}

	public String getStringRepresentation() {
		return this.stringRepresentation;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public EfficientString getEdgeString(Map<String, Variable> variables) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canConsume(EfficientString string) {
		if ( string.codePointLength() == 0 ) {
			return false;
		}
		if ( string.codePointLength() > 1 ) {
			throw new AssertionError( "string's codePointLength was greater than 1" );
		}
		return this.criterion.intersects( string.codePoint( 0 ) );
	}

	@Override
	public boolean canConsume(MatchInfo matchInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isSet() {
		return true;
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
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SetState setState = (SetState) o;

		if ( idx != setState.idx ) {
			return false;
		}
		if ( length != setState.length ) {
			return false;
		}
		return !(stringRepresentation != null ?
				!stringRepresentation.equals( setState.stringRepresentation ) :
				setState.stringRepresentation != null);

	}

}
