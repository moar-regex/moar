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

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.NonDeterministicException;
import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.parser.RegexLexer;
import com.github.s4ke.moar.regex.parser.RegexParser;
import com.github.s4ke.moar.regex.parser.RegexTreeListener;
import com.github.s4ke.moar.strings.EfficientString;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Martin Braun
 */
public class ParserTest {

	@Test
	public void test() {
		MoaPattern pattern = MoaPattern.compile( "^Deterministic|OrNot$" );
		MoaMatcher matcher = pattern.matcher( "Deterministic" );
		assertTrue( matcher.nextMatch() );
		assertTrue( matcher.matches() );
	}

	@Test
	public void testTusker() {
		//from: http://tusker.org/regex/regex_benchmark.html
		//adapted the syntax though
		//FIXME: fix syntax of our regexes to be more compatible?
		//or just throw out the PCRE syntax alltogether in favour
		//of a more readable alternative
		{
			Regex regex = parseRegex( "^(([^:]+)://)?([^:/]+)(:([0-9]+))?(/.*)" );
			TestUtil.assertNonDet( regex );
		}

		{
			//this is non deterministic (see the start)
			Regex regex = parseRegex( "(([^:]+)://)?([^:/]+)(:([0-9]+))?(/.*)" );
			TestUtil.assertNonDet( regex );
		}

		{
			//the original has this bounded by word: \\b(\\w+)(\\s+\\1)+\\b
			//this is non deterministic by our current definition
			Regex regex = parseRegex( "(\\w+)(\\s+\\1)+" );
			TestUtil.assertNonDet( regex );
		}

		{
			//adapted the [+-] to [\\+\\-] (our ANTLR grammar is not that clever
			//we also treat the dot as the any metachar.
			Regex regex = parseRegex( "usd [\\+\\-]?[0-9]+\\.[0-9][0-9]" );
			TestUtil.assertDet( regex );
			Moa moa = regex.toMoa();
			TestUtil.assertMatch( true, moa, "usd 1234.00" );
			TestUtil.assertMatch( true, moa, "usd +1234.00" );
			TestUtil.assertMatch( true, moa, "usd -1234.00" );
			TestUtil.assertMatch( true, moa, "usd 1.00" );
			TestUtil.assertMatch( true, moa, "usd +1.00" );
			TestUtil.assertMatch( true, moa, "usd -1.00" );
			TestUtil.assertMatch( false, moa, "1234.00" );
			TestUtil.assertMatch( false, moa, "usd .00" );
			TestUtil.assertMatch( false, moa, "usd +.00" );
			TestUtil.assertMatch( false, moa, "usd -.00" );
			TestUtil.assertMatch( false, moa, "usd 1." );
		}
	}


	@Test
	public void testTooHarshDeterminism() {
		//these were wrongly identified as non deterministic
		//but this was only wrong for non matching groups
		TestUtil.assertDet( parseRegex( "(?:)|(?:)" ) );
		TestUtil.assertNonDet( parseRegex( "()|()" ) );

		TestUtil.assertDet( parseRegex( "(?:a+)+" ) );
		TestUtil.assertNonDet( parseRegex( "(a+)+" ) );

		TestUtil.assertDet( parseRegex( "(?:a|(?:))+" ) );
		TestUtil.assertNonDet( parseRegex( "(a|())+" ) );
	}

	@Test
	public void testCaret() {
		Moa moa = parseRegex( "^a" ).toMoa();
		assertMatch( true, moa, "a" );
		{
			String tmp = "aa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 1, cnt );
		}
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}
	}

	@Test
	public void testDollar() {
		Moa moa = parseRegex( "a$" ).toMoa();
		assertMatch( true, moa, "a" );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "ab";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 1, cnt );
		}
	}

	@Test
	public void testEndOfLastMatch() {
		Moa moa = parseRegex( "\\Ga" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			String tmp = "aa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 2, cnt );
		}

		{
			String tmp = "a a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 1, cnt );
		}
	}

	@Test
	public void testEndOfInput() {
		Moa moa = parseRegex( "a\\z" ).toMoa();
		assertTrue( moa.check( "a" ) );

		{
			String tmp = "aa";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			Assert.assertEquals( 1, cnt );
		}
	}

	@Test
	public void testCaretAndDollar() {
		Moa moa = parseRegex( "^a$" ).toMoa();
		assertTrue( moa.check( "a" ) );
		for ( EfficientString eff : BoundConstants.LINE_BREAK_CHARS ) {
			String tmp = "a" + eff.toString() + "a";
			MoaMatcher matcher = moa.matcher( tmp );
			int cnt = 0;
			while ( matcher.nextMatch() ) {
				++cnt;
			}
			assertEquals( 2, cnt );
		}
	}

	@Test
	public void testUTF32() {
		Moa moa;
		{
			int[] codePoints = "someStuff~\uD801\uDC28~someOtherStuff".codePoints().toArray();
			moa = parseRegex( new String( codePoints, 0, codePoints.length ) ).toMoa();
		}
		{
			int[] codePoints = "someStuff\uD801\uDC28someOtherStuff".codePoints().toArray();
			assertMatch( true, moa, new String( codePoints, 0, codePoints.length ) );
		}
	}

	@Test
	public void testSingleChar() {
		Regex regex = parseRegex( "a" );
		assertMatch( true, regex, "a" );
		assertMatch( false, regex, "b" );
		assertMatch( false, regex, "" );
	}

	@Test
	public void testSimpleConcat() {
		Regex regex = parseRegex( "abc" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "abc" ) );
		assertFalse( moa.check( "ab" ) );
	}

	@Test
	public void testSimpleOr() {
		Regex regex = parseRegex( "a|b" );
		assertMatch( true, regex, "a" );
		assertMatch( true, regex, "b" );
		assertMatch( false, regex, "c" );
		assertMatch( false, regex, "" );
	}

	@Test
	public void testSimpleGroup() {
		Regex regex = parseRegex( "(a)" );
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		{
			MoaMatcher matcher = moa.matcher( "a" );
			assertTrue( matcher.nextMatch() );
			assertTrue( matcher.matches() );
			assertEquals( "a", matcher.getVariableContent( 1 ) );
			assertEquals( "a", matcher.getVariableContent( "1" ) );
		}
	}

	@Test
	public void testSimplePositiveSet() {
		Regex regex = parseRegex( "[abc]" );
		assertMatch( true, regex, "a" );
		assertMatch( true, regex, "b" );
		assertMatch( true, regex, "c" );
		assertMatch( false, regex, "d" );
		assertMatch( false, regex, "" );
	}
	
	@Test
	public void testAStarBStarStar() {
		Regex regex = parseRegex( "(?:a*b*)*" );
		TestUtil.assertDet( regex );
	}

	@Test
	public void testRangePositiveSet() {
		Moa moa = parseRegex( "[ac-zAC-Z]" ).toMoa();
		for ( char c = 'a'; c <= 'z'; ++c ) {
			if ( c == 'b' ) {
				assertMatch( false, moa, String.valueOf( c ) );
			}
			else {
				assertMatch( true, moa, String.valueOf( c ) );
			}
		}
		for ( char c = 'A'; c <= 'Z'; ++c ) {
			if ( c == 'B' ) {
				assertMatch( false, moa, String.valueOf( c ) );
			}
			else {
				assertMatch( true, moa, String.valueOf( c ) );
			}
		}
		assertMatch( false, moa, "!" );
	}

	@Test
	public void testRangeNegativeset() {
		//reuse this, the non-determinism check is quite expensive if a set
		//is used
		Moa moa = parseRegex( "[^a]" ).toMoa();
		for ( int i = 0; i <= Character.MAX_VALUE; ++i ) {
			if ( i == 'a' ) {
				assertMatch( false, moa, String.valueOf( (char) i ) );
			}
			else {
				assertMatch( true, moa, String.valueOf( (char) i ) );
			}
		}
	}

	@Test
	public void testNumericalRegex() {
		Moa moa = parseRegex( "1" ).toMoa();
		assertMatch( true, moa, "1" );
	}

	@Test
	public void testBackRefIndexed() {
		Moa moa = parseRegex( "(a*)b\\1" ).toMoa();
		assertMatch( false, moa, "ab" );
		assertMatch( true, moa, "aba" );
		assertMatch( false, moa, "aaba" );
		assertMatch( false, moa, "abaa" );
		assertMatch( true, moa, "aabaa" );
	}

	@Test
	public void testBackRefNamed() {
		Moa moa = parseRegex( "(?<zA1>a*)b\\k<zA1>" ).toMoa();
		assertMatch( false, moa, "ab" );
		assertMatch( true, moa, "aba" );
		assertMatch( false, moa, "aaba" );
		assertMatch( false, moa, "abaa" );
		assertMatch( true, moa, "aabaa" );
	}

	@Test
	public void testNonCapturingGroup() {
		Moa moa = parseRegex( "(?:ab|d)*c" ).toMoa();
		assertMatch( true, moa, "c" );
		assertMatch( true, moa, "abc" );
		assertMatch( true, moa, "ababc" );
		assertMatch( true, moa, "abdabc" );
	}

	@Test
	public void testEscapeSimple() {
		Regex regex = parseRegex( "\\^" );
		assertMatch( true, regex, "^" );
	}

	@Test
	public void testEscapeBackslash() {
		Regex regex = parseRegex( "\\\\" );
		assertMatch( true, regex, "\\" );
	}

	@Test
	public void testOrNothing() {
		Moa moa = parseRegex( "a?" ).toMoa();
		assertMatch( true, moa, "" );
		assertMatch( true, moa, "a" );
		assertMatch( false, moa, "b" );
	}

	@Test
	public void testPrecedenceAndOr() {
		Regex regex = parseRegex( "ab|bcd" );
		Moa moa = regex.toMoa();
		assertMatch( true, moa, "ab" );
		assertMatch( true, moa, "bcd" );
		assertMatch( false, moa, "abbcd" );
	}

	@Test
	public void testEpsilon() {
		Moa moa = parseRegex( "" ).toMoa();
		assertMatch( true, moa, "" );
		assertMatch( false, moa, "a" );
	}

	@Test
	public void testWhitespace() {
		Moa moa = parseRegex( "\\s" ).toMoa();
		assertMatch( true, moa, " " );
		assertMatch( false, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testNonWhitespace() {
		Moa moa = parseRegex( "\\S" ).toMoa();
		assertMatch( false, moa, " " );
		assertMatch( true, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testEmail() {
		Moa moa = parseRegex( "\\w+@\\w\\w+\\.\\w\\w+" ).toMoa();
		assertMatch( true, moa, "test@email.com" );
		assertMatch( false, moa, "test.com" );
	}

	@Test
	public void testWordCharacter() {
		Moa moa = parseRegex( "\\w" ).toMoa();
		assertMatch( false, moa, " " );
		assertMatch( true, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testNonWordCharacter() {
		Moa moa = parseRegex( "\\W" ).toMoa();
		assertMatch( true, moa, " " );
		assertMatch( false, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testNumeric() {
		Moa moa = parseRegex( "\\d" ).toMoa();
		assertMatch( true, moa, "1" );
		assertMatch( false, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testNonNumeric() {
		Moa moa = parseRegex( "\\D" ).toMoa();
		assertMatch( false, moa, "1" );
		assertMatch( true, moa, "a" );
		assertMatch( false, moa, "" );
	}

	@Test
	public void testAny() {
		Moa moa = parseRegex( "." ).toMoa();
		assertMatch( false, moa, "" );
		assertMatch( true, moa, "a" );
	}

	@Test
	public void testSpecialChars() {
		char[] specialChars = {'s', 'S', 'w', 'W', 'd', 'D', 'k'};
		for ( char specialChar : specialChars ) {
			Moa moa = parseRegex( String.valueOf( specialChar ) ).toMoa();
			assertMatch( false, moa, "" );
			assertMatch( true, moa, String.valueOf( specialChar ) );
		}
	}

	@Test
	public void testDeterminismPreMoa() {
		//The one that failed while Dominik tested the cli tool
		Regex regex = parseRegex(
				"(?<1>)(?<2>)(?<3>)(?<4>)(?<5>)(?<6>)(?<7>)(?<8>)(?<9>)(?<10>)(?<11>)(?<12>)(?<13>)(?<14>)(?<15>)(?<16>)(?<17>)(?<18>)(?<19>)(?<20>)" +
						"a" +
						"(()|(?<1>))(()|(?<2>))(()|(?<3>))(()|(?<4>))(()|(?<5>))(()|(?<6>))(()|(?<7>))(()|(?<8>))(()|(?<9>))(()|(?<10>))(()|(?<11>))(()|(?<12>))(()|(?<13>))(()|(?<14>))(()|(?<15>))(()|(?<16>))(()|(?<17>))(()|(?<18>))(()|(?<19>))(()|(?<20>))" +
						"b"
		);
		try {
			regex.toMoa();
			fail( "failed to recognize a non deterministic regex as non deterministic" );
		}
		catch (NonDeterministicException e) {
			System.out.println( "successfully got Exception while building the MOA: " + e.getMessage() );
		}
	}

	@Test
	public void testEscapeNonBrackets() {
		char[] escapees = {'*', '+', '?', '\\', '.' | '$'};
		for ( char escapee : escapees ) {
			{
				Moa moa = parseRegex( "\\" + escapee ).toMoa();
				assertMatch( false, moa, "" );
				assertMatch( true, moa, String.valueOf( escapee ) );
			}
		}

		Regex regex = parseRegex( "\\^" );
		assertMatch( true, regex, "^" );
	}

	private static final String COOL_REGEX = "((?<y>\\k<x>)(?<x>\\k<y>a))+";
	private static final String COOL_REGEX_2 = "(?<x>)((?<y>\\k<x>)(?<x>\\k<y>a))+";

	private static final String[] COOL_REGEXES = {COOL_REGEX, COOL_REGEX_2};

	@Test
	public void testCool() {
		for ( String regexStr : COOL_REGEXES ) {
			Regex regex = parseRegex( regexStr );
			Moa moa = regex.toMoa();
			assertTrue( moa.check( "aaaa" ) );
			boolean tmp = false;
			for ( int i = 0; i < 100; ++i ) {
				String str = repeat( "a", i );
				boolean res = moa.check( str );
				if ( res ) {
					tmp = true;
					System.out.println( str );
				}
			}
			assertTrue( tmp );
		}
	}

	@Test
	public void testSpecialCharsWithoutEscaping() {
		for ( String str : Arrays.asList( ":", "<", ">" ) ) {
			Regex regex = parseRegex( str );
			Moa moa = regex.toMoa();
			assertTrue( moa.check( str ) );
		}
	}

	@Test
	public void testCoolLanguagesJava() {
		for ( String regexStr : COOL_REGEXES ) {
			try {
				{
					Pattern pattern = Pattern.compile( regexStr );
					boolean tmp = false;
					for ( int i = 0; i < 100; ++i ) {
						String str = repeat( "a", i );
						boolean res = pattern.matcher( str ).matches();
						if ( res ) {
							tmp = true;
							System.out.println( str );
						}
					}
					assertTrue( tmp );
				}
			}
			catch (PatternSyntaxException e) {
				System.out.println( "Java Pattern doesn't like " + regexStr + ", with message: " + e.getMessage() );
			}
		}
	}

	private static String repeat(String str, int times) {
		String ret = "";
		for ( int i = 0; i < times; ++i ) {
			ret += str;
		}
		return ret;
	}

	@Test
	public void testEscapeBrackets() {
		char[] brackets = {'[', ']', '(', ')'};
		for ( char bracket : brackets ) {
			{
				Moa moa = parseRegex( "\\" + bracket ).toMoa();
				assertMatch( false, moa, "" );
				assertMatch( true, moa, String.valueOf( bracket ) );
			}
		}
	}

	private static void assertMatch(boolean shouldMatch, Regex regex, String input) {
		assertEquals( shouldMatch, regex.toMoa().check( input ) );
	}

	private static void assertMatch(boolean shouldMatch, Moa moa, String input) {
		assertEquals( shouldMatch, moa.check( input ) );
	}

	private static RegexParser regexParser(String regexStr) {
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( regexStr ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		return parser;
	}

	private static Regex parseRegex(String regexStr) {
		RegexParser parser = regexParser( regexStr );
		RegexParser.RegexContext regexTree = parser.regex();
		System.out.println( regexTree.toStringTree( parser ) );
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		return listener.finalRegex();
	}

}
