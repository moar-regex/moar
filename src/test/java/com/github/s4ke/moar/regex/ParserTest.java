package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.MoaMatcher;
import com.github.s4ke.moar.regex.parser.RegexLexer;
import com.github.s4ke.moar.regex.parser.RegexParser;
import com.github.s4ke.moar.regex.parser.RegexTreeListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class ParserTest {

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
			assertTrue( matcher.checkAsSingleWord() );
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
	public void testBackRef() {
		Moa moa = parseRegex( "(a*)b\\1" ).toMoa();
		assertMatch( false, moa, "ab" );
		assertMatch( true, moa, "aba" );
		assertMatch( false, moa, "aaba" );
		assertMatch( false, moa, "abaa" );
		assertMatch( true, moa, "aabaa" );
	}

	@Test
	public void testEscape() {
		regexParser( "\\\\" ).charOrEscaped();

		Regex regex = parseRegex( "\\\\" );
		assertMatch( true, regex, "\\" );
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
		System.out.println( regexTree.toStringTree(parser) );
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		return listener.finalRegex();
	}

}
