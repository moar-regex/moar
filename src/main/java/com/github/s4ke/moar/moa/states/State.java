package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

/**
 * @author Martin Braun
 */
public interface State {

	int getIdx();

	EfficientString getEdgeString(Map<String, Variable> variables);

	boolean canConsume(EfficientString string);

	boolean canConsume(MatchInfo matchInfo);

	boolean isStatic();

	boolean isSet();

	boolean isVariable();

	boolean isBound();

	void touch();

}
