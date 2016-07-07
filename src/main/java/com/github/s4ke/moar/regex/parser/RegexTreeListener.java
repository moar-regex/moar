package com.github.s4ke.moar.regex.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.github.s4ke.moar.regex.Regex;
import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class RegexTreeListener extends RegexBaseListener implements RegexListener {

	private final Stack<Regex> regexStack = new Stack<>();
	private int groupCount = 0;

	public Regex finalRegex() {
		return this.regexStack.peek();
	}

	private static String getCh(RegexParser.CharOrEscapedContext charOrEscaped) {
		if ( charOrEscaped.CHAR() != null ) {
			return charOrEscaped.CHAR().getText();
		}
		else {
			return charOrEscaped.METACHAR().getText();
		}
	}

	@Override
	public void exitRegex(RegexParser.RegexContext ctx) {
		if ( this.regexStack.size() != 1 ) {
			throw new AssertionError();
		}
	}

	@Override
	public void exitBackRef(RegexParser.BackRefContext ctx) {
		Regex regex = Regex.reference( ctx.NUMBER().getText() );
		this.regexStack.push( regex );
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
			Regex regex = Regex.set( (char) 0, Character.MAX_VALUE );
			this.regexStack.push( regex );
		}
		else if ( ctx.charOrEscaped() != null ) {
			Regex regex = Regex.str( getCh( ctx.charOrEscaped() ) );
			this.regexStack.push( regex );
		}
		else if ( ctx.EOS() != null ) {
			//FIXME: handle this!!
		}
	}

	@Override
	public void exitGroup(RegexParser.GroupContext ctx) {
		if ( ctx.union() == null ) {
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
			regex = Regex.str( getCh( setItem.charOrEscaped() ) );

			setItems = setItems.setItems();
		}

		while ( setItems != null ) {
			RegexParser.SetItemContext setItem = setItems.setItem();
			if ( setItem.charOrEscaped() != null ) {
				regex = regex.or( Regex.str( getCh( setItem.charOrEscaped() ) ) );
			}
			else if ( setItem.range() != null ) {
				char from = getCh( setItem.range().charOrEscaped( 0 ) ).charAt( 0 );
				char to = getCh( setItem.range().charOrEscaped( 1 ) ).charAt( 0 );

				//the explicit set function is really only needed here, and
				//not above as single tokens in a group can be handled with a simple
				//or without any real memory usage difference
				Regex rangeRegex = Regex.set( from, to );
				regex = regex.or( rangeRegex );
			}

			//go on with the next one
			setItems = setItems.setItems();
		}

		this.regexStack.push( regex );
	}

	@Override
	public void exitNegativeSet(RegexParser.NegativeSetContext ctx) {
		Set<EfficientString> excluded = new HashSet<>();
		{
			RegexParser.SetItemsContext setItems = ctx.setItems();
			while ( setItems != null ) {
				RegexParser.SetItemContext setItem = setItems.setItem();
				if ( setItem.charOrEscaped() != null ) {
					excluded.add( new EfficientString( getCh( setItems.setItem().charOrEscaped() ) ) );
				}
				else if ( setItem.range() != null ) {
					char from = getCh( setItem.range().charOrEscaped( 0 ) ).charAt( 0 );
					char to = getCh( setItem.range().charOrEscaped( 1 ) ).charAt( 0 );

					for ( int i = from; i <= to; ++i ) {
						excluded.add( new EfficientString( String.valueOf( (char) i ) ) );
					}
				}
				setItems = setItems.setItems();
			}
		}

		this.regexStack.push( Regex.set( (string) -> !excluded.contains( string ) ) );
	}

}
