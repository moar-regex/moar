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
package com.github.s4ke.moar.benchmark;

import com.github.s4ke.moar.util.CharSeq;
import com.github.s4ke.moar.util.IntCharSeq;

/**
 * @author Martin Braun
 */
public class Regex {

	public static final String[] REGEX_TO_BENCH = new String[] {
			"th(e)\\1+",
			"fairest",
			"from",
			"beauty",
			"foe",
			"f((riend)|(oe))",
			"[A-Z]([a-z])+",
			"shall besiege",
			"(c)?old"
	};

	public static final String[] EASY_MATCHES = new String[] {
			"the",
			"thee",
			"fairest",
			"from",
			"beauty",
			"foe",
			"friend",
			"Asdfwqekadkweiqkdkqew",
			"shall besiege",
			"cold",
			"old"
	};

	public static final String[] TWO_TO_POWER_OF_N_AND_OTHERS = new String[128];
	public static final CharSeq[] TWO_TO_POWER_OF_N_AND_OTHERS_CHARSEQ = new CharSeq[128];
	static {
		StringBuilder builder = new StringBuilder( "a" );
		//so that we start at 2 a's in arr[0]
		for(int i = 0; i < TWO_TO_POWER_OF_N_AND_OTHERS.length; ++i) {
			String curStr = builder.append( "a" ).toString();
			TWO_TO_POWER_OF_N_AND_OTHERS[i] = curStr;
			TWO_TO_POWER_OF_N_AND_OTHERS_CHARSEQ[i] = new IntCharSeq( curStr );
		}
	}

	public static final String TWO_TO_POWER_OF_N_MOA = "(a(\\1)+)";

}
