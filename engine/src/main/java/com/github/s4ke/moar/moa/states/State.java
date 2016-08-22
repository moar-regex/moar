package com.github.s4ke.moar.moa.states;

import java.util.Map;

import com.github.s4ke.moar.strings.EfficientString;

/**
 * Interface that represents the States in a Memory Occurence Automaton
 *
 * @author Martin Braun
 */
public interface State {

	/**
	 * @return the idx of this state in the underlying {@link com.github.s4ke.moar.moa.edgegraph.EdgeGraph}.
	 * This is an implementation detail that is needed for faster access.
	 */
	int getIdx();

	/**
	 * can only be used if either {@link State#isVariable()} or {@link State#isStatic()} returns true
	 *
	 * @param variables the current state of the variables
	 *
	 * @return the string that has to be read if the MOA is allowed to go to this state
	 */
	EfficientString getEdgeString(Map<String, Variable> variables);

	/**
	 * can only be used if either {@link State#isSet()} or {@link State#isStatic()} returns true
	 *
	 * @param string the {@link EfficientString} to check
	 *
	 * @return true if this State can "consume" the input
	 */
	boolean canConsume(EfficientString string);

	/**
	 * can only be used if {@link State#isBound()} returns true.
	 *
	 * @param matchInfo the current Matching State
	 *
	 * @return true if this State can "consume" the input
	 */
	boolean canConsume(MatchInfo matchInfo);

	/**
	 * @return true if this is a {@link BasicState}
	 */
	boolean isStatic();

	/**
	 * @return true if this is a {@link SetState}
	 */
	boolean isSet();

	/**
	 * @return true if this is a {@link VariableState}
	 */
	boolean isVariable();

	/**
	 * @return true if this is a {@link BoundState}
	 */
	boolean isBound();

	/**
	 * called when the state is "touched" while running the MOA.
	 */
	void touch();

}
