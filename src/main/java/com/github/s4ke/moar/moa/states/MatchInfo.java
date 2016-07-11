package com.github.s4ke.moar.moa.states;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public class MatchInfo {

	private EfficientString string;
	private int pos;
	private boolean matchedLast;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public boolean isMatchedLast() {
		return matchedLast;
	}

	public void setMatchedLast(boolean matchedLast) {
		this.matchedLast = matchedLast;
	}

	public EfficientString getString() {

		return string;
	}

	public void setString(EfficientString string) {
		this.string = string;
	}
}
