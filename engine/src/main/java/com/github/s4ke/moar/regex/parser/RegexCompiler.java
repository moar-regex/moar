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
package com.github.s4ke.moar.regex.parser;

import com.github.s4ke.moar.regex.Regex;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * ANTLR based Regex compiler (end users should probably use {@link com.github.s4ke.moar.MoaPattern#compile(String)} instead of manually using this class)
 *
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
		StringBuilder additionalMessage = new StringBuilder();
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
						additionalMessage.append( "SyntaxEception in Regex: \"" )
								.append( regexStr )
								.append( "\": " )
								.append( msg );
						if ( offendingSymbol instanceof CommonToken ) {
							CommonToken token = (CommonToken) offendingSymbol;
							if ( token.getText().equals( "*" ) || token.getText().equals( "+" ) || token.getText()
									.equals( "?" ) ) {
								additionalMessage.append( ", dangling metacharacter: '" )
										.append( ((CommonToken) offendingSymbol).getText() )
										.append( "' at line " )
										.append( token.getLine() )
										.append( ", pos " )
										.append( token.getCharPositionInLine() );
							}
						}
					}
				}
		);
		RegexParser.RegexContext regexTree = parser.regex();
		RegexTreeListener listener = new RegexTreeListener();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk( listener, regexTree );
		if ( parser.getNumberOfSyntaxErrors() > 0 ) {
			throw new IllegalArgumentException( "malformed regex found : " + regexStr + "\n" + additionalMessage.toString() );
		}
		return listener.finalRegex();
	}

}
