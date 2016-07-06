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
		Regex regex = parseRegex( "[ac-zAC-Z]" );
		for ( char c = 'a'; c <= 'Z'; ++c ) {
			if ( c == 'c' || c == 'C' ) {
				assertMatch( false, regex, String.valueOf( c ) );
			}
			else {
				assertMatch( true, regex, String.valueOf( c ) );
			}
		}
		assertMatch( false, regex, "!" );
	}

	@Test
	public void testEscape() {
		Regex regex = parseRegex( "\\\\" );
		//assertMatch( true, regex, "\\" );
	}

	private static void assertMatch(boolean shouldMatch, Regex regex, String input) {
		assertEquals( shouldMatch, regex.toMoa().check( input ) );
	}

	private static Regex parseRegex(String regexStr) {
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( regexStr ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		RegexParser.RegexContext regexTree = parser.regex();
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		return listener.finalRegex();
	}

}
