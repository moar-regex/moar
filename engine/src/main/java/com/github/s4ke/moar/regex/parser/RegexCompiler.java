package com.github.s4ke.moar.regex.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.s4ke.moar.regex.Regex;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Martin Braun
 */
public final class RegexCompiler {

	private static final Logger LOGGER = Logger.getLogger( RegexCompiler.class.getName() );

	private static RegexParser regexParser(String regexStr) {
		RegexLexer lexer = new RegexLexer( new ANTLRInputStream( regexStr ) );
		RegexParser parser = new RegexParser( new CommonTokenStream( lexer ) );
		parser.setBuildParseTree( true );
		return parser;
	}

	public static Regex compile(String regexStr) {
		RegexParser parser = regexParser( regexStr );
		parser.getErrorListeners().clear();
		parser.addErrorListener(
				new BaseErrorListener() {
					@Override
					public void syntaxError(
							Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e) {
						LOGGER.log( Level.WARNING, "SyntaxEception in Regex: \"" + regexStr + "\": " + msg );
					}
				}
		);
		RegexParser.RegexContext regexTree = parser.regex();
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		if ( parser.getNumberOfSyntaxErrors() > 0 ) {
			throw new IllegalArgumentException( "malformed regex: " + regexStr );
		}
		return listener.finalRegex();
	}

}
