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

import com.github.s4ke.moar.strings.EfficientString;
import com.github.s4ke.moar.util.CharSeq;

/**
 * @author Martin Braun
 */
public class MatchInfo {

	private EfficientString string;
	private CharSeq wholeString;
	private int pos = 0;
	private int lastMatch = -1;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public EfficientString getString() {
		return string;
	}

	public CharSeq getWholeString() {
		return wholeString;
	}

	public void setWholeString(CharSeq wholeString) {
		this.wholeString = wholeString;
	}

	public void setString(EfficientString string) {
		this.string = string;
	}

	public int getLastMatch() {
		return this.lastMatch;
	}

	public void setLastMatch(int lastMatch) {
		this.lastMatch = lastMatch;
	}
}
