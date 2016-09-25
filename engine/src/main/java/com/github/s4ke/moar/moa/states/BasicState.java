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
