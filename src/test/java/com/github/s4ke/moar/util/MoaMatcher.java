package com.github.s4ke.moar.util;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.regex.Regex;

/**
 * @author Martin Braun
 */
public class MoaMatcher implements GenericMatcher {

	private final Moa moa;

	public MoaMatcher(Moa moa) {
		this.moa = moa;
	}

	public MoaMatcher(Regex regex) {
		this.moa = regex.toMoa();
	}

	@Override
	public boolean check(String str) {
		return this.moa.check( str );
	}

	@Override
	public String toString() {
		return "MoaMatcher{" +
				"moa=" + moa +
				'}';
	}
}
