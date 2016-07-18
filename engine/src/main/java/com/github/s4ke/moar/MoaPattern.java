package com.github.s4ke.moar;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.parser.RegexCompiler;

/**
 * @author Martin Braun
 */
public final class MoaPattern {

	private final Moa moa;

	private MoaPattern(Moa moa) {
		this.moa = moa;
	}

	public static MoaPattern compile(String regexStr) {
		return new MoaPattern( RegexCompiler.compile( regexStr ).toMoa() );
	}

	public MoaMatcher matcher(CharSequence str) {
		return this.moa.matcher( str );
	}

}
