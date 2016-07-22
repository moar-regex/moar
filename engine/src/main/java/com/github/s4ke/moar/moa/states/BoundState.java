package com.github.s4ke.moar.moa.states;

import java.util.Map;
import java.util.function.Function;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class BoundState implements State {

	private final int idx;
	private final String boundHandled;
	private final Function<MatchInfo, Boolean> condition;

	public BoundState(int idx, String boundHandled, Function<MatchInfo, Boolean> condition) {
		this.idx = idx;
		this.condition = condition;
		this.boundHandled = boundHandled;
	}

	public String getBoundHandled() {
		return this.boundHandled;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canConsume(MatchInfo matchInfo) {
		return this.condition.apply( matchInfo );
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
		return false;
	}

	@Override
	public boolean isBound() {
		return true;
	}

	@Override
	public void touch() {

	}
}
