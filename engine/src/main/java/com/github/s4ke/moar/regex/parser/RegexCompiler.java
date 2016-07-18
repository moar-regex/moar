package com.github.s4ke.moar.regex.parser;

import com.github.s4ke.moar.regex.Regex;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Martin Braun
 */
public final class RegexCompiler {

	private static RegexParser regexParser(String regexStr) {
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( regexStr ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		return parser;
	}

	public static Regex compile(String regexStr) {
		RegexParser parser = regexParser( regexStr );
		RegexParser.RegexContext regexTree = parser.regex();
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		return listener.finalRegex();
	}

}
