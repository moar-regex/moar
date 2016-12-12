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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class JavaMoaEqualityTest {

	private static final String someSonnet = "                     1\n" +
			"  From fairest creatures we desire increase,\n" +
			"  That thereby beauty's rose might never die,\n" +
			"  But as the riper should by time decease,\n" +
			"  His tender heir might bear his memory:\n" +
			"  But thou contracted to thine own bright eyes,\n" +
			"  Feed'st thy light's flame with self-substantial fuel,\n" +
			"  Making a famine where abundance lies,\n" +
			"  Thy self thy foe, to thy sweet self too cruel:\n" +
			"  Thou that art now the world's fresh ornament,\n" +
			"  And only herald to the gaudy spring,\n" +
			"  Within thine own bud buriest thy content,\n" +
			"  And tender churl mak'st waste in niggarding:\n" +
			"    Pity the world, or else this glutton be,\n" +
			"    To eat the world's due, by the grave and thee.";

	public static final String[] REGEX_TO_CHECK = new String[] {
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

	@Test
	public void testSimple() {
		assertTrue(Pattern.compile( "th(e)\\1+" ).matcher( " thee." ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1+" ).matcher( " thee." ).nextMatch());

		assertTrue(Pattern.compile( "th(e)\\1+" ).matcher( " thethee" ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1+" ).matcher( " thethee" ).nextMatch());

		assertTrue(Pattern.compile( "th(e)\\1*" ).matcher( " thethee" ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1*" ).matcher( " thethee" ).nextMatch());

		assertTrue(Pattern.compile( "th(e)\\1+" ).matcher( someSonnet ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1+" ).matcher( someSonnet ).nextMatch());

		assertTrue(Pattern.compile( "th(e)\\1*" ).matcher( someSonnet ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1*" ).matcher( someSonnet ).nextMatch());
	}

	@Test
	public void testWeird() {
		assertTrue(Pattern.compile( "th(e)\\1ater" ).matcher( " theeater" ).find());
		assertTrue(MoaPattern.compile( "th(e)\\1ater" ).matcher( " theeater" ).nextMatch());
	}

	@Test
	public void testEqualityFullText() {
		int matchCountFirst = 0;
		for ( String str : REGEX_TO_CHECK ) {
			Pattern pattern = Pattern.compile( str );
			Matcher matcher = pattern.matcher( someSonnet );
			while ( matcher.find() ) {
				++matchCountFirst;
			}
		}

		int matchCountSnd = 0;
		for ( String str : REGEX_TO_CHECK ) {
			MoaPattern pattern = MoaPattern.compile( str );
			MoaMatcher matcher = pattern.matcher( someSonnet );
			while ( matcher.nextMatch() ) {
				++matchCountSnd;
			}
		}

		assertEquals( matchCountFirst, matchCountSnd );
	}


}
