package com.github.s4ke.moar.moa;

/**
 * @author Martin Braun
 */
public class BasicState implements State {

	public int idx;
	public final String string;

	public BasicState(int idx, String string) {
		this.idx = idx;
		this.string = string;
	}

	@Override
	public int getIdx() {
		return this.idx;
	}

	@Override
	public String getEdgeString() {
		return this.string;
	}

	@Override
	public boolean isTerminal() {
		return true;
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
}
