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

	public SetState(int idx, int length, Function<EfficientString, Boolean> criterion) {
		this.idx = idx;
		this.length = length;
		this.criterion = criterion;
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
	public void touch() {

	}
}
