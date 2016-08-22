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
package com.github.s4ke.moar;

import com.github.s4ke.moar.util.CharSeq;

/**
 * Similar to Java Patterns this encapsulates the
 * automaton state during matching. These objects
 * are usually much more lightweight, but can still
 * be reused for minimum object creation overhead.
 *
 * @author Martin Braun
 */
public interface MoaMatcher {

	/**
	 * same as {@link MoaMatcher#reuse(CharSeq)} but with a {@link com.github.s4ke.moar.util.IntCharSeq} to represent
	 * basic Java CharSequences
	 * @param str the CharSequence that this object is to be used with
	 * @return this (for chaining purposes)
	 */
	MoaMatcher reuse(CharSequence str);

	/**
	 * reuse this instance without recreating it
	 * @param seq the CharSeq that this object is to be used with
	 * @return this (for chaining purposes)
	 */
	MoaMatcher reuse(CharSeq seq);

	/**
	 * replaces the first match of the underlying {@link MoaPattern} with the given String
	 * and returns the result
	 * @param replacement the String to replace the match with
	 * @return the resulting String
	 */
	String replaceFirst(String replacement);

	/**
	 * replaces all matches of the underlying {@link MoaPattern} with the given String
	 * and returns the result
	 * @param replacement the string to replace the matches with
	 * @return the resulting string
	 */
	String replaceAll(String replacement);

	/**
	 * @return the start of the current match
	 */
	int getStart();

	/**
	 * @return the end of the current match (exclusive)
	 */
	int getEnd();

	/**
	 * goes through the input and searches for the next match
	 * (maximum length) the underlying {@link MoaPattern} can produce.
	 * The next call of this method will start at the end of the last match
	 * (if any)
	 *
	 * @return true iff there if there was a match
	 */
	boolean nextMatch();

	/**
	 * treats the whole input atomically and tries to match the underlying {@link MoaPattern} against it
	 */
	boolean matches();

	/**
	 * @param occurence the variable occurence index to return (1-based)
	 * @return the contents of the variable as a String
	 * @throws IllegalArgumentException if Variable is non-existent
	 */
	String getVariableContent(int occurence);

	/**
	 * @param name the name of the variable to return
	 * @return the contents of the variable as a String
	 * @throws IllegalArgumentException if Variable is non-existent
	 */
	String getVariableContent(String name);

}
