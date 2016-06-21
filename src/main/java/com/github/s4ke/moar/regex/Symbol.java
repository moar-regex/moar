package com.github.s4ke.moar.regex;

/**
 * @author Martin Braun
 */
final class Symbol {

	public final String symbol;
	public final Integer number;

	Symbol(String symbol, Integer number) {
		this.symbol = symbol;
		this.number = number;
	}

	@Override
	public String toString() {
		return "{" + this.symbol + "_" + this.number + "}";
	}
}
