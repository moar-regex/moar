/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class VariableState implements State {

	private final int idx;
	private final String variableName;

	public VariableState(int idx, String variableName) {
		this.idx = idx;
		this.variableName = variableName;
	}

	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public EfficientString getEdgeString(Map<String, Variable> variables) {
		return variables.get( this.variableName ).getEdgeString();
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
		return false;
	}

	@Override
	public boolean isSet() {
		return false;
	}

	@Override
	public boolean isVariable() {
		return true;
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
		return "VariableState{" +
				"idx=" + idx +
				", variableName=" + variableName +
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

		VariableState that = (VariableState) o;

		if ( idx != that.idx ) {
			return false;
		}
		return !(variableName != null ? !variableName.equals( that.variableName ) : that.variableName != null);

	}

}
