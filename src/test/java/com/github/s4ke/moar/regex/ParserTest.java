package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
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
			junit.framework.Assert.assertEquals( 2, cnt );
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
	public void testEscapeNonBrackets() {
		char[] escapees = {'*', '+', '?', ':', '\\', '.' | '$'};
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

	@Test
	public void testEscapeBrackets() {
		char[] brackets = {'[', ']', '(', ')', '{', '}', '<', '>'};
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
