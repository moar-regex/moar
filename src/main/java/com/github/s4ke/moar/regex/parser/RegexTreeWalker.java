package com.github.s4ke.moar.regex.parser;

import java.util.Stack;

import com.github.s4ke.moar.regex.Regex;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author Martin Braun
 */
public class RegexTreeWalker implements RegexListener {

	private final Stack<Regex> regexStack = new Stack<>();

	@Override
	public void enterRegex(RegexParser.RegexContext ctx) {
		ctx.union().enterRule( this );
	}

	@Override
	public void exitRegex(RegexParser.RegexContext ctx) {
		if ( this.regexStack.size() != 1 ) {
			throw new AssertionError();
		}
	}

	@Override
	public void enterUnion(RegexParser.UnionContext ctx) {
		boolean additionalPop = false;
		if ( ctx.union() != null ) {
			ctx.union().enterRule( this );
			additionalPop = true;
		}
		ctx.concatenation().enterRule( this );
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = regex.or( this.regexStack.pop() );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitUnion(RegexParser.UnionContext ctx) {

	}

	@Override
	public void enterConcatenation(RegexParser.ConcatenationContext ctx) {
		boolean additionalPop = false;
		if ( ctx.concatenation() != null ) {
			ctx.concatenation().enterRule( this );
			additionalPop = true;
		}
		ctx.basicRegex().enterRule( this );
		Regex regex = this.regexStack.pop();
		if ( additionalPop ) {
			regex = regex.and( this.regexStack.pop() );
		}
		this.regexStack.push( regex );
	}

	@Override
	public void exitConcatenation(RegexParser.ConcatenationContext ctx) {

	}

	@Override
	public void enterBasicRegex(RegexParser.BasicRegexContext ctx) {
		if ( ctx.star() != null ) {
			ctx.star().enterRule( this );
		}
		else if ( ctx.plus() != null ) {
			ctx.plus().enterRule( this );
		}
		else if ( ctx.elementaryRegex() != null ) {
			ctx.elementaryRegex().enterRule( this );
		}
	}

	@Override
	public void exitBasicRegex(RegexParser.BasicRegexContext ctx) {

	}

	@Override
	public void enterStar(RegexParser.StarContext ctx) {
		ctx.elementaryRegex().enterRule( this );
		Regex regex = this.regexStack.pop();
		regex = regex.star();
		this.regexStack.push( regex );
	}

	@Override
	public void exitStar(RegexParser.StarContext ctx) {

	}

	@Override
	public void enterPlus(RegexParser.PlusContext ctx) {
		ctx.elementaryRegex().enterRule( this );
		Regex regex = this.regexStack.pop();
		regex = regex.plus();
		this.regexStack.push( regex );
	}

	@Override
	public void exitPlus(RegexParser.PlusContext ctx) {

	}

	@Override
	public void enterElementaryRegex(RegexParser.ElementaryRegexContext ctx) {
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
		} else if(ctx.EOS() != null) {
			//FIXME: handle this!!
		}
	}

	@Override
	public void exitElementaryRegex(RegexParser.ElementaryRegexContext ctx) {

	}

	@Override
	public void enterGroup(RegexParser.GroupContext ctx) {

	}

	@Override
	public void exitGroup(RegexParser.GroupContext ctx) {

	}

	@Override
	public void enterSet(RegexParser.SetContext ctx) {

	}

	@Override
	public void exitSet(RegexParser.SetContext ctx) {

	}

	@Override
	public void enterPositiveSet(RegexParser.PositiveSetContext ctx) {

	}

	@Override
	public void exitPositiveSet(RegexParser.PositiveSetContext ctx) {

	}

	@Override
	public void enterNegativeSet(RegexParser.NegativeSetContext ctx) {

	}

	@Override
	public void exitNegativeSet(RegexParser.NegativeSetContext ctx) {

	}

	@Override
	public void enterSetItems(RegexParser.SetItemsContext ctx) {

	}

	@Override
	public void exitSetItems(RegexParser.SetItemsContext ctx) {

	}

	@Override
	public void enterSetItem(RegexParser.SetItemContext ctx) {

	}

	@Override
	public void exitSetItem(RegexParser.SetItemContext ctx) {

	}

	@Override
	public void enterRange(RegexParser.RangeContext ctx) {

	}

	@Override
	public void exitRange(RegexParser.RangeContext ctx) {

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
