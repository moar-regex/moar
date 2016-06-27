package com.github.s4ke.moar.moa;

import com.github.s4ke.moar.util.EfficientString;

/**
 * @author Martin Braun
 */
public interface State {

	int getIdx();

	EfficientString getEdgeString();

	boolean isTerminal();

	void touch();

}
