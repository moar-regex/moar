package com.github.s4ke.moar.moa.states;

import com.github.s4ke.moar.strings.EfficientString;
import com.github.s4ke.moar.util.CharSeq;

/**
 * @author Martin Braun
 */
public class MatchInfo {

	private EfficientString string;
	private CharSeq wholeString;
	private int pos = 0;
	private int lastMatch = -1;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public EfficientString getString() {
		return string;
	}

	public CharSeq getWholeString() {
		return wholeString;
	}

	public void setWholeString(CharSeq wholeString) {
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
