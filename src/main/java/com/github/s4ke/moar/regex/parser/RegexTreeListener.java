package com.github.s4ke.moar.regex.parser;

import java.util.Stack;

import com.github.s4ke.moar.regex.Regex;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author Martin Braun
 */
public class RegexTreeListener extends RegexBaseListener implements RegexListener {

	private final Stack<Regex> regexStack = new Stack<>();
	private int groupCount = 0;

	public Regex finalRegex() {
		return this.regexStack.peek();
	}

	@Override
	public void exitRegex(RegexParser.RegexContext ctx) {
		if ( this.regexStack.size() != 1 ) {
			throw new AssertionError();
		}
	}

	@Override
	public void exitUnion(RegexParser.UnionContext ctx) {
		boolean additionalPop = false;
		if ( ctx.union() != null ) {
			additionalPop = true;
		}
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = this.regexStack.pop().or( regex );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitConcatenation(RegexParser.ConcatenationContext ctx) {
		boolean additionalPop = false;
		if ( ctx.concatenation() != null ) {
			additionalPop = true;
		}
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = this.regexStack.pop().and( regex );
		}
		this.regexStack.push( regex );
	}


	@Override
	public void exitBasicRegex(RegexParser.BasicRegexContext ctx) {
		//no-op
	}

	@Override
	public void exitStar(RegexParser.StarContext ctx) {
		Regex regex = this.regexStack.pop();
		regex = regex.star();
		this.regexStack.push( regex );
	}

	@Override
	public void exitPlus(RegexParser.PlusContext ctx) {
		Regex regex = this.regexStack.pop();
		regex = regex.plus();
		this.regexStack.push( regex );
	}

	@Override
	public void exitElementaryRegex(RegexParser.ElementaryRegexContext ctx) {
		if ( ctx.ANY() != null ) {
			//FIXME: woah, this is bad
			Regex regex = Regex.str( String.valueOf( Character.valueOf( (char) 0 ) ) );
			for ( char i = 1; i < Character.MAX_VALUE; ++i ) {
				regex.or( String.valueOf( Character.valueOf( i ) ) );
			}
			regex = Regex.str( String.valueOf( Character.valueOf( Character.MAX_VALUE ) ) );
			this.regexStack.push( regex );
		}
		else if ( ctx.CHAR() != null ) {
			Regex regex = Regex.str( ctx.CHAR().getSymbol().getText() );
			this.regexStack.push( regex );
		}
		else if ( ctx.EOS() != null ) {
			//FIXME: handle this!!
		}
	}

	@Override
	public void exitGroup(RegexParser.GroupContext ctx) {
		if ( ctx.regex() == null ) {
			this.regexStack.push( Regex.eps().bind( String.valueOf( ++this.groupCount ) ) );
		}
		else {
			Regex regex = this.regexStack.pop();
			regex = regex.bind( String.valueOf( ++this.groupCount ) );
			this.regexStack.push( regex );
		}
	}

	@Override
	public void exitPositiveSet(RegexParser.PositiveSetContext ctx) {
		RegexParser.SetItemsContext setItems = ctx.setItems();
		Regex regex;
		{
			RegexParser.SetItemContext setItem = setItems.setItem();
			regex = Regex.str( setItem.CHAR().getText() );

			setItems = setItems.setItems();
		}

		while ( setItems != null ) {
			RegexParser.SetItemContext setItem = setItems.setItem();
			if ( setItem.CHAR() != null ) {
				regex = regex.or( Regex.str( setItem.CHAR().getText() ) );
			}
			else if ( setItem.range() != null ) {
				char from = setItem.range().CHAR( 0 ).getText().charAt( 0 );
				char to = setItem.range().CHAR( 1 ).getText().charAt( 0 );

				Regex rangeRegex = Regex.str( String.valueOf( from ) );
				for ( int i = from + 1; i <= to; ++i ) {
					rangeRegex.or( Regex.str( String.valueOf( (char) i ) ) );
				}
				regex = regex.or( rangeRegex );
			}

			//go on with the next one
			setItems = setItems.setItems();
		}

		this.regexStack.push( regex );
	}

	@Override
	public void exitNegativeSet(RegexParser.NegativeSetContext ctx) {

	}

	@Override
	public void visitTerminal(TerminalNode terminalNode) {

	}

	@Override
	public void visitErrorNode(ErrorNode errorNode) {

	}

	@Override
	public void enterEveryRule(ParserRuleContext parserRuleContext) {

	}

	@Override
	public void exitEveryRule(ParserRuleContext parserRuleContext) {

	}
}
