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
package com.github.s4ke.moar.moa.edgegraph;

import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
public enum ActionType {
	OPEN {
		@Override
		public void act(String variableName, Variable val) {
			if ( !val.isOpen() ) {
				val.open();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "o(%s)", variableName );
		}
	},
	CLOSE {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.close();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "c(%s)", variableName );
		}
	},
	RESET {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.contents.reset();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "r(%s)", variableName );
		}
	};

	public static ActionType fromString(String str) {
		switch ( str ) {
			case "o":
				return OPEN;
			case "c":
				return CLOSE;
			case "r":
				return RESET;
			default:
				throw new IllegalArgumentException( "unrecognized ActionType String representation: " + str );
		}
	}

	public abstract void act(String variableName, Variable val);

	public abstract String toString(String variableName);
}
