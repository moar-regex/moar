package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * @author Martin Braun
 */
public interface State {

	int getIdx();

	EfficientString getEdgeString(Map<String, Variable> variables);

	boolean canConsume(EfficientString string);

	boolean isStatic();

	boolean isSet();

	boolean isVariable();

	void touch();

}
