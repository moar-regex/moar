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
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.strings.EfficientString;

import org.junit.Test;
import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class MoaWithDSLTest {

	@Test
	public void testStartOfLine() {
		Moa moa = Regex.caret().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			MoaMatcher matcher = moa.matcher( "aa" );
			assertTrue( matcher.nextMatch() );
			assertFalse( matcher.nextMatch() );
		}
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ab";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 2, cnt );
		}
	}

	@Test
	public void testEndOfLastMatch() {
		Moa moa = Regex.endOfLastMatch().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			String tmp = "aa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}

		{
			String tmp = "a a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 1, cnt );
		}
	}

	@Test
	public void testEndOfInput() {
		Moa moa = Regex.str( "aa" ).end().toMoa();
		assertTrue( moa.check( "aa" ) );

		{
			String tmp = "aaaa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			org.junit.Assert.assertEquals( 1, cnt );
		}
	}

	@Test
	public void testStartAndEndOfLine() {
		Moa moa = Regex.caret().and( "a" ).dollar().toMoa();
		assertTrue( moa.check( "a" ) );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 2, cnt );
		}
	}

	@Test
	public void testStartOfLineSingleLine() {
		Moa moa = Regex.caret().and( "a" ).toMoa();
		assertTrue( moa.check( "a" ) );
	}

	@Test
	public void testStartOfLineDeterminism() {
		TestUtil.assertDet( Regex.caret().and( "a" ) );
		TestUtil.assertNonDet( Regex.caret().or( "a" ).and( "b" ) );
	}

	@Test
	public void testEndOfLine() {
		Moa moa = Regex.str( "a" ).dollar().toMoa();
		TestUtil.assertMatch( true, moa, "a" );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ab";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 1, cnt );
		}

		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ba";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}
	}

	@Test
	public void testEndOfLineDeterminism() {
		TestUtil.assertDet( Regex.str( "a" ).dollar() );
		TestUtil.assertNonDet( Regex.str( "a" ).or( Regex.dollar_() ) );
	}

	@Test
	public void testSimple() {
		Regex regex = Regex.str( "a" );
		assertTrue( regex.toMoa().check( "a" ) );
	}

	@Test
	public void testLongerString() {
		Moa moa = Regex.str( "test" ).toMoa();
		assertTrue( moa.check( "test" ) );
	}

	@Test
	public void testOr() {
		Regex regex = Regex.str( "a" ).or( "b" ).or( "" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "b" ) );
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "Z" ) );
	}

	@Test
	public void testCompilcatedOr() {
		Regex regex = Regex.str( "a" ).and( Regex.str( "b" ).or( Regex.eps() ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertFalse( moa.check( "Z" ) );
		assertTrue( moa.check( "ab" ) );
		assertFalse( moa.check( "abc" ) );
	}

	@Test
	public void testOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "b" ) );
	}

	@Test
	public void testBiggerOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).and( Regex.str( "b" ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "ab" ) );
		assertTrue( moa.check( "b" ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void testAnd() {
		Moa moa = Regex.str( "a" ).and( "b" ).toMoa();
		assertTrue( moa.check( "ab" ) );
		assertFalse( moa.check( "a" ) );
		assertFalse( moa.check( "" ) );
		assertFalse( moa.check( "ba" ) );
	}

	@Test
	public void testSimpleBinding() {
		Regex regex = Regex.str( "a" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) );
		Moa moa = regex.toMoa();
		{
			MoaMatcher matcher = moa.matcher( "a|a" );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( 1 ) );
			assertFalse( moa.check( "a|aa" ) );
		}
		{
			MoaMatcher matcher = moa.matcher( "a|aa" );
			assertFalse( matcher.matches() );
		}
	}

	@Test
	public void testOrInBinding() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a|a" ) );
		assertTrue( moa.check( "|" ) );
		assertFalse( moa.check( "|a" ) );
		assertFalse( moa.check( "a|" ) );
	}

	@Test
	public void testOrEpsWithAnd() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).and( "b" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "ab" ) );
		assertTrue( moa.check( "b" ) );
	}

	@Test
	public void testStarInBinding() {
		Regex regex = Regex.str( "a" ).star().bind( "x" )
				.and( "|" )
				.and( Regex.reference( "x" ) );
		Moa moa = regex.toMoa();
		for ( int i = 0; i < 10; ++i ) {
			String str = TestUtil.repeat( "a", i ) + "|" + TestUtil.repeat( "a", i );
			assertTrue( str + " was not accepted by x{a*}|&x", moa.check( str ) );
		}
	}

	@Test
	public void testMatchEmptyStringAsFullText() {
		Regex regex = Regex.eps();
		Moa moa = regex.toMoa();
		int matchCount = 0;
		MoaMatcher matcher = moa.matcher( "" );
		while ( matcher.nextMatch() ) {
			++matchCount;
			if ( matchCount > 100 ) {
				break;
			}
		}
		assertEquals( 1, matchCount );
	}

	@Test
	public void testEndOfInputOnEmptyString() {
		Regex regex = Regex.end_();
		Moa moa = regex.toMoa();
		int matchCount = 0;
		MoaMatcher matcher = moa.matcher( "" );
		while ( matcher.nextMatch() ) {
			++matchCount;
			if ( matchCount > 100 ) {
				break;
			}
		}
		assertEquals( 1, matchCount );
	}

	@Test
	public void testStarInBindingMoreComplex() {
		Regex regex = Regex.str( "a" ).or( "b" ).star().bind( "x" )
				.and( "|" )
				.and( Regex.reference( "x" ) );
		Moa moa = regex.toMoa();
		for ( int i = 0; i < 10; ++i ) {
			String str = TestUtil.repeat( "ab", i ) + "|" + TestUtil.repeat( "ab", i );
			assertTrue( str + " was not accepted by x{(a or b)*}|&x*", moa.check( str ) );
		}
	}

	@Test
	public void testPlusAroundBinding() {
		Regex regex = Regex.str( "a" ).plus().bind( "x" )
				.and( "b" )
				.plus();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "aab" ) );
		assertTrue( moa.check( "aabab" ) );
	}

	@Test
	public void testWhiteSpace() {
		Regex regex = Regex.whiteSpace();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( " " ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void plusInBinding() {
		Moa moa = Regex.str( "a" ).plus().bind( "toast" ).and( "b" ).and( Regex.reference( "toast" ) ).toMoa();
		assertTrue( moa.check( "aaabaaa" ) );
		assertFalse( moa.check( "aaaba" ) );
		assertFalse( moa.check( "aaab" ) );
	}

	@Test
	public void testSimplePlus() {
		Moa moa = Regex.str( "a" ).plus().toMoa();
		assertFalse( moa.check( "" ) );
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "aa" ) );
	}

	@Test
	public void testCool() {
		Moa moa = Regex.reference( "x" )
				.bind( "y" )
				.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
				.plus()
				.toMoa();
		assertTrue( moa.check( "aaaa" ) );
		boolean tmp = false;
		for ( int i = 0; i < 100; ++i ) {
			String str = TestUtil.repeat( "a", i );
			boolean res = moa.check( str );
			if ( res ) {
				tmp = true;
				System.out.println( str );
			}
		}
		assertTrue( tmp );
	}

	@Test
	public void testWhoopDieDoo() {
		MoaPattern pattern = MoaPattern.compile( Regex.caret().and( "a" )
														 .and( Regex.set( "A", "Z" )
																	   .or( Regex.str( "c" ).plus() )
																	   .bind( "x" ) )
														 .and( "a" )
														 .and(
																 Regex.reference( "x" ) ) );
		assertTrue( pattern.matcher( "aBaB" ).matches() );
		assertTrue( pattern.matcher( "accacc" ).matches() );
		assertFalse( pattern.matcher( "aBac" ).matches() );
		assertFalse( pattern.matcher( "aCCaCC" ).matches() );
		assertFalse( pattern.matcher( "accac" ).matches() );
	}

	@Test
	public void testDontMergeBindings() {
		MoaPattern pattern = MoaPattern.compile( Regex.str( "a" )
														 .bind( "x" )
														 .and( Regex.str( "b" ).bind( "x" ) )
														 .and( Regex.reference( "x" ) ) );
		assertTrue( pattern.matcher( "abb" ).matches() );
		assertFalse( pattern.matcher( "abab" ).matches() );
	}

	@Test
	public void testTwoToThePowerOfN() {
		MoaPattern pattern = MoaPattern.compile( Regex.str( "a" ).and( Regex.reference( "x" ).plus() )
														 .bind( "x" ) );
		assertFalse( pattern.matcher( "a" ).matches() );
		assertTrue( pattern.matcher( "aa" ).matches() );
		assertTrue( pattern.matcher( "aaaa" ).matches() );
		assertFalse( pattern.matcher( "aaaaa" ).matches() );
		assertFalse( pattern.matcher( "aaaaaa" ).matches() );
		assertFalse( pattern.matcher( "aaaaaaa" ).matches() );
		assertTrue( pattern.matcher( "aaaaaaaa" ).matches() );
	}

	@Test
	public void testOrEpsilonWithBind() {
		MoaPattern pattern = MoaPattern.compile( Regex.str( "a" ).bind( "x" ).or( Regex.eps() ) );
		{
			MoaMatcher matcher = pattern.matcher( "a" );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( "x" ) );
		}

		{
			MoaMatcher matcher = pattern.matcher( "" );
			assertTrue( matcher.matches() );
			assertEquals( "", matcher.getVariableContent( "x" ) );
		}

	}

	@Test
	public void testEpsilonInBind() {
		MoaPattern pattern = MoaPattern.compile( Regex.str( "a" ).or( Regex.eps() ).bind( "x" ) );
		{
			MoaMatcher matcher = pattern.matcher( "a" );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( "x" ) );
		}

		{
			MoaMatcher matcher = pattern.matcher( "" );
			assertTrue( matcher.matches() );
			assertEquals( "", matcher.getVariableContent( "x" ) );
		}
	}

	@Test
	public void testPlusOrEpsilonWithBind() {
		MoaPattern pattern = MoaPattern.compile( Regex.str( "a" ).plus().bind( "x" ).or( Regex.eps() ) );
		{
			MoaMatcher matcher = pattern.matcher( "a" );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( "x" ) );
		}

		{
			MoaMatcher matcher = pattern.matcher( "aa" );
			assertTrue( matcher.matches() );
			assertEquals( "aa", matcher.getVariableContent( "x" ) );
		}

		{
			MoaMatcher matcher = pattern.matcher( "" );
			assertTrue( matcher.matches() );
			assertEquals( "", matcher.getVariableContent( "x" ) );
		}

	}

	@Test
	public void testPlusWithBind() {
		Regex regex = Regex.str( "abc" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) ).plus();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "abc|abc" ) );
		assertFalse( moa.check( "abc|abcabc" ) );
		//ensure that the internal regex is reused
		assertTrue( moa.check( "abc|abcabc|abc" ) );
		//ensure that the bind from before is used
		assertFalse( moa.check( "abc|abca|a" ) );
	}

	@Test
	public void testSet() {
		Regex regex = Regex.set( 'a', 'c' );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "b" ) );
		assertTrue( moa.check( "c" ) );
		assertFalse( moa.check( "d" ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void testTooHarshDeterminism() {
		TestUtil.assertDet( Regex.eps().or( Regex.eps() ) );
		TestUtil.assertNonDet( Regex.eps().bind( "1" ).or( Regex.eps().bind( "2" ) ) );

		TestUtil.assertDet( Regex.str( "a" ).plus().plus() );
		TestUtil.assertNonDet( Regex.str( "a" ).plus().bind( "2" ).plus() );

		TestUtil.assertDet( Regex.str( "a" ).or( Regex.eps() ).plus() );
		TestUtil.assertNonDet( Regex.str( "a" ).or( Regex.eps().bind( "2" ) ).bind( "1" ).plus() );
	}

	@Test
	public void testNonExistingReference() {
		Regex regex = Regex.reference( "x" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "" ) );
	}

	@Test
	public void testNotDeterministic() {
		TestUtil.assertNonDet( Regex.str( "a" ).bind( "x" ).or( "a" ) );
		TestUtil.assertNonDet( Regex.str( "a" ).or( "b" ).plus().bind( "x" ).and( Regex.reference( "x" ) ) );
		TestUtil.assertDet( Regex.str( "a" ).or( "b" ).plus().bind( "x" ).and( "c" ).and( Regex.reference( "x" ) ) );

		{
			//this is deterministic
			TestUtil.assertDet( Regex.str( "a" ).plus().or( Regex.eps() ) );
			//but this is not as we can't know whether to bind epsilon or not
			TestUtil.assertNonDet( Regex.str( "a" ).star().bind( "x" ).or( Regex.eps() ) );
			//the binding makes the above non deterministic
			TestUtil.assertDet( Regex.str( "a" ).star().or( Regex.eps() ) );
		}

		TestUtil.assertDet( Regex.str( "a" ).bind( "x" ).plus() );
		TestUtil.assertNonDet( Regex.str( "a" ).plus().bind( "x" ).plus() );
		TestUtil.assertDet( Regex.str( "a" ).plus().bind( "x" ).and( "b" ).plus() );
		TestUtil.assertNonDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( Regex.reference( "x" ).and( Regex.reference( "y" ) ) )
		);
		TestUtil.assertDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( "c" ).and( Regex.reference( "x" ).and( Regex.reference( "y" ) ) )
		);
		TestUtil.assertNonDet(
				Regex.str( "a" )
						.plus()
						.bind( "x" )
						.and( Regex.str( "b" ).plus().bind( "y" ) )
						.or( Regex.str( "b" ).plus().bind( "y" ).and( Regex.str( "a" ).plus().bind( "x" ) ) )
						.and( Regex.reference( "x" ).or( Regex.reference( "y" ) ) )
		);
		TestUtil.assertDet(
				Regex.reference( "x" )
						.bind( "y" )
						.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
						.plus()

		);
	}

	@Test
	public void testDeterminismWithRange() {
		TestUtil.assertNonDet( Regex.set( "a", "c" ).or( "a" ) );
		TestUtil.assertNonDet( Regex.set( "a", "c" ).or( Regex.set( "a", "c" ) ) );
	}

	@Test
	public void testEpsilonMoa() {
		Moa moa = Regex.eps().toMoa();
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "a" ) );
	}

}
