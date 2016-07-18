package com.github.s4ke.moar;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.parser.RegexCompiler;

/**
 * @author Martin Braun
 */
public final class MoaPattern {

	private final Moa moa;
	private final String regex;

	private MoaPattern(Moa moa, String regex) {
		this.moa = moa;
		this.regex = regex;
	}

	public static MoaPattern compile(String regexStr) {
		try {
			return new MoaPattern( RegexCompiler.compile( regexStr ).toMoa(), regexStr );
		}
		catch (NonDeterministicException e) {
			throw new NonDeterministicException( "The regex \"" + regexStr + "\" is not deterministic", e );
		}
	}

	public MoaMatcher matcher(CharSequence str) {
		return this.moa.matcher( str );
	}

	@Override
	public String toString() {
		return this.regex;
	}
}
