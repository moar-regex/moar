/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.s4ke.moar.moa;

import com.github.s4ke.moar.util.EfficientString;

/**
 * @author Martin Braun
 */
public class VariableState implements State {

	private final int idx;
	private final Variable variable;
	private String contents;
	private boolean touched = false;

	public VariableState(int idx, Variable variable) {
		this.idx = idx;
		this.variable = variable;
	}

	public Variable getVariable() {
		return this.variable;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public EfficientString getEdgeString() {
		return this.variable.getEdgeString();
	}

	@Override
	public boolean isTerminal() {
		return false;
	}

	public boolean varOpen() {
		return this.variable.isOpen();
	}

	public void reset() {
		this.touched = false;
		this.contents = null;
	}

	@Override
	public void touch() {
		//FIXME: store all contents in a list or maybe
		//we only store the value in the variable?
		if ( !this.touched ) {
			this.contents = variable.contents.toString();
			this.touched = true;
		}
	}

	public String getContents() {
		return this.contents;
	}

	@Override
	public String toString() {
		return "VariableState{" +
				"idx=" + idx +
				", variable=" + variable +
				", contents='" + contents + '\'' +
				", touched=" + touched +
				'}';
	}
}
