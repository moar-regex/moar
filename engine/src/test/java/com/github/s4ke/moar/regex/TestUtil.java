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
package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;

import org.junit.Assert;

import static org.junit.Assert.fail;

/**
 * @author Martin Braun
 */
public class TestUtil {
	public static String repeat(String str, int times) {
		String ret = "";
		for ( int i = 0; i < times; ++i ) {
			ret += str;
		}
		return ret;
	}

	private static void assertMatch(boolean shouldMatch, Regex regex, String input) {
		Assert.assertEquals( shouldMatch, regex.toMoa().check( input ) );
	}

	public static void assertMatch(boolean shouldMatch, Moa moa, String input) {
		Assert.assertEquals( shouldMatch, moa.check( input ) );
	}

	public static void assertNonDet(Regex regex) {
		try {
			regex.toMoa();
			fail( "regex " + regex + " was not recognized as non-deterministic" );
		}
		catch (NonDeterministicException e) {
			System.out.println( "successfully got Exception while building the MOA: " + e.getMessage() );
		}
	}

	public static void assertDet(Regex regex) {
		regex.toMoa();
	}
}
