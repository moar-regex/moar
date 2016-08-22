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

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.moa.Moa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class FullTextTest {

	@Test
	public void testMultiLine() {
		{
			Regex regex = Regex.str( "toast" ).or( "or is it?" ).bind( "x" );
			Moa moa = regex.toMoa();
			MoaMatcher matcher = moa.matcher( "toast is not a beverage\nno wait, or is it?\nb" );
			int matchCount = 0;
			while ( matcher.nextMatch() ) {
				++matchCount;
				assertTrue( moa.matcher( matcher.getVariableContent( 1 ) ).matches() );
			}
			assertEquals( 2, matchCount );
		}
	}

	@Test
	public void testCoolLanguage() {
		Regex regex = Regex.reference( "x" )
				.bind( "y" )
				.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
				.plus().bind( "all" );
		System.out.println( regex.toString() );
		Moa moa = regex.toMoa();
		{
			MoaMatcher matcher = moa.matcher( "aaaa" );
			assertTrue( matcher.nextMatch() );
			assertEquals( "aaaa", matcher.getVariableContent( "all" ) );
		}
		{
			MoaMatcher matcher = moa.matcher( "aaaaa" );
			assertFalse( moa.check( "aaaaa" ) );
			assertTrue( matcher.nextMatch() );
			assertEquals( "aaaa", matcher.getVariableContent( "all" ) );
		}
	}

}
