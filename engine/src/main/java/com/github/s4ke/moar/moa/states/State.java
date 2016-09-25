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
 * Interface that represents the States in a Memory Occurence Automaton
 *
 * @author Martin Braun
 */
public interface State {

	/**
	 * @return the idx of this state in the underlying {@link com.github.s4ke.moar.moa.edgegraph.EdgeGraph}.
	 * This is an implementation detail that is needed for faster access.
	 */
	int getIdx();

	/**
	 * can only be used if either {@link State#isVariable()} or {@link State#isStatic()} returns true
	 *
	 * @param variables the current state of the variables
	 *
	 * @return the string that has to be read if the MOA is allowed to go to this state
	 */
	EfficientString getEdgeString(Map<String, Variable> variables);

	/**
	 * can only be used if either {@link State#isSet()} or {@link State#isStatic()} returns true
	 *
	 * @param string the {@link EfficientString} to check
	 *
	 * @return true if this State can "consume" the input
	 */
	boolean canConsume(EfficientString string);

	/**
	 * can only be used if {@link State#isBound()} returns true.
	 *
	 * @param matchInfo the current Matching State
	 *
	 * @return true if this State can "consume" the input
	 */
	boolean canConsume(MatchInfo matchInfo);

	/**
	 * @return true if this is a {@link BasicState}
	 */
	boolean isStatic();

	/**
	 * @return true if this is a {@link SetState}
	 */
	boolean isSet();

	/**
	 * @return true if this is a {@link VariableState}
	 */
	boolean isVariable();

	/**
	 * @return true if this is a {@link BoundState}
	 */
	boolean isBound();

}
