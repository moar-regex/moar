package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
interface StateContributor {

	void contributeStates(
			Map<String, Variable> variables, Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant, Supplier<Integer> idxSupplier);

}
