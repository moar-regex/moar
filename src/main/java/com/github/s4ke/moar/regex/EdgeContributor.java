package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.moa.edgegraph.EdgeGraph;
import com.github.s4ke.moar.moa.states.State;
import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
interface EdgeContributor {

	void contributeEdges(
			EdgeGraph edgeGraph, Map<String, Variable> variables, Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant);

}
