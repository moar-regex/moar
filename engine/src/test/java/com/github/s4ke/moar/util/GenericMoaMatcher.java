package com.github.s4ke.moar.util;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.Regex;

/**
 * @author Martin Braun
 */
public class GenericMoaMatcher implements GenericMatcher {

	private final Moa moa;

	public GenericMoaMatcher(Moa moa) {
		this.moa = moa;
	}

	public GenericMoaMatcher(Regex regex) {
		this.moa = regex.toMoa();
	}

	@Override
	public boolean check(String str) {
		return this.moa.check( str );
	}

	@Override
	public String toString() {
		return "GenericMoaMatcher{" +
				"moa=" + moa +
				'}';
	}
}
