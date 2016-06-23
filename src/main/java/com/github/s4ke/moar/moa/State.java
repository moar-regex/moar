package com.github.s4ke.moar.moa;

/**
 * @author Martin Braun
 */
public interface State {

	int getIdx();

	String getEdgeString();

	boolean isTerminal();

	void touch();

}
