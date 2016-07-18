package com.github.s4ke.moar.regex;

/**
 * @author Martin Braun
 */
final class Symbol {

	public final String symbol;

	Symbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return "{" + this.symbol + "}";
	}

}
