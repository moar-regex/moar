package com.github.s4ke.moar.moa.edgegraph;

import com.github.s4ke.moar.moa.states.State;

/**
 * @author Martin Braun
 */
public interface CurStateHolder {

	State getState();

	void setState(State state);

}
