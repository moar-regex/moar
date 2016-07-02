package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.regex.parser.RegexLexer;
import com.github.s4ke.moar.regex.parser.RegexParser;
import com.github.s4ke.moar.regex.parser.RegexTreeWalker;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.Test;

/**
 * @author Martin Braun
 */
public class Parser {

	@Test
	public void testSimple() {
		String src = "ab|([a-z])*";
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( src ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		RegexParser.RegexContext regex = parser.regex();
		RegexTreeWalker walker = new RegexTreeWalker();
		regex.enterRule( walker );
	}

}
