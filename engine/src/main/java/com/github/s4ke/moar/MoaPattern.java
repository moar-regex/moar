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

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.Regex;
import com.github.s4ke.moar.regex.parser.RegexCompiler;
import com.github.s4ke.moar.util.Accessor;
import com.github.s4ke.moar.util.CharSeq;

/**
 * Deterministic Regexes with BackReferences using {@link Moa}'s.
 * Works with CodePoints so that matching against UTF-32 Strings is
 * possible.
 *
 * @author Martin Braun
 */
public final class MoaPattern {

	private final Moa moa;
	private final String regex;

	private MoaPattern(Moa moa, String regex) {
		this.moa = moa;
		this.regex = regex;
	}

	/**
	 * @return the underlying regex as a String or null if no String representation is available
	 */
	public String getRegex() {
		return this.regex;
	}

	/**
	 * compiles the given Regex String into a {@link MoaPattern}.
	 *
	 * @param regexStr the Regex String to parse into a {@link MoaPattern}
	 *
	 * @return the {@link MoaPattern} that represents the given Regex String
	 */
	public static MoaPattern compile(String regexStr) {
		try {
			return new MoaPattern( RegexCompiler.compile( regexStr ).toMoa(), regexStr );
		}
		catch (NonDeterministicException e) {
			throw new NonDeterministicException( "The regex \"" + regexStr + "\" is not deterministic", e );
		}
	}

	/**
	 * compiles the given Regex into a {@link MoaPattern}.
	 *
	 * @param regex the Regex to compile into a {@link MoaPattern}
	 *
	 * @return the {@link MoaPattern} that represents the given Regex
	 */
	public static MoaPattern compile(Regex regex) {
		return new MoaPattern( regex.toMoa(), regex.toString() );
	}

	/**
	 * constructs a {@link MoaPattern} from a manually built {@link Moa}
	 *
	 * @param moa the underlying Moa to use with this {@link MoaPattern}
	 * @param regex the Regex String for description purposes
	 *
	 * @return the {@link MoaPattern} that uses the given {@link Moa}
	 */
	public static MoaPattern build(Moa moa, String regex) {
		return new MoaPattern( moa, regex );
	}

	/**
	 * same as {@link MoaPattern#matcher(CharSequence)} but with native Java CharSequences
	 *
	 * @param str the CharSequence to match against
	 *
	 * @return the resulting {@link MoaMatcher}
	 */
	public MoaMatcher matcher(CharSequence str) {
		return this.moa.matcher( str );
	}

	/**
	 * constructs a {@link MoaMatcher} that uses the {@link Moa} represented by this object
	 * for matching against the given {@link CharSeq}
	 *
	 * @param seq the CharSeq to match against
	 *
	 * @return the resulting {@link MoaMatcher}
	 */
	public MoaMatcher matcher(CharSeq seq) {
		return this.moa.matcher( seq );
	}

	/**
	 * <b>EXPERTS-ONLY</b>
	 * direct access to the underlying {@link Moa}
	 *
	 * @param accessor the accessor Function
	 */
	public void accessMoa(Accessor<Moa> accessor) {
		accessor.access( this.moa );
	}

	@Override
	public String toString() {
		return this.regex;
	}

}
