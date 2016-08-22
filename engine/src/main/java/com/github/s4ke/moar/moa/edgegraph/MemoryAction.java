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

import java.util.Map;

import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
public class MemoryAction {

	public static final MemoryAction NO_OP = null;

	public final ActionType actionType;
	public final String variable;

	public MemoryAction(ActionType actionType, String variable) {
		this.actionType = actionType;
		this.variable = variable;
	}

	public void act(Map<String, Variable> variables) {
		Variable val = variables.get( this.variable );
		if ( val == null ) {
			throw new AssertionError( "variable with name " + this.variable + " not found" );
		}
		this.actionType.act( this.variable, val );
	}

	@Override
	public String toString() {
		return "var=" + this.variable +
				"action=" + this.actionType;
	}
}
