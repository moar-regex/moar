package com.github.s4ke.moar.moa.states;

import java.util.Map;
import java.util.function.Function;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class SetState implements State {

	public final int idx;
	public final int length;
	public final Function<EfficientString, Boolean> criterion;
	public final String stringRepresentation;

	public SetState(int idx, int length, Function<EfficientString, Boolean> criterion, String stringRepresentation) {
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
		return this.criterion.apply( string );
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
