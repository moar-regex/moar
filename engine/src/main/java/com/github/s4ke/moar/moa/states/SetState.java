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
	public String toString() {
		if(this.stringRepresentation != null) {
			return this.stringRepresentation;
		} else {
			return this.criterion.toString();
		}
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
