package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public interface State {

	int getIdx();

	EfficientString getEdgeString(Map<String, Variable> variables);

	boolean isTerminal();

	void touch();

}
