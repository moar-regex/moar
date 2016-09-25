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
package com.github.s4ke.moar.moa.states;

import java.util.Map;
import java.util.function.Function;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * Implementation of {@link State} that represents boundary matches like the start of input or the end of input.
 *
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
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		BoundState that = (BoundState) o;

		if ( idx != that.idx ) {
			return false;
		}
		return !(boundHandled != null ? !boundHandled.equals( that.boundHandled ) : that.boundHandled != null);

	}

	@Override
	public String toString() {
		return "BoundState{" +
				"idx=" + idx +
				", boundHandled='" + boundHandled + '\'' +
				", condition=" + condition +
				'}';
	}
}
