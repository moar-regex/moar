package com.github.s4ke.moar.moa.states;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class MatchInfo {

	private EfficientString string;
	private CharSequence wholeString;
	private int pos;
	private int lastMatch;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public EfficientString getString() {
		return string;
	}

	public CharSequence getWholeString() {
		return wholeString;
	}

	public void setWholeString(CharSequence wholeString) {
		this.wholeString = wholeString;
	}

	public void setString(EfficientString string) {
		this.string = string;
	}

	public int getLastMatch() {
		return lastMatch;
	}

	public void setLastMatch(int lastMatch) {
		this.lastMatch = lastMatch;
	}
}
