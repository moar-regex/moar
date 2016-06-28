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

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public EfficientString getEdgeString(Map<String, Variable> variables) {
		return variables.get(this.variableName).getEdgeString();
	}

	@Override
	public boolean isTerminal() {
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
}
