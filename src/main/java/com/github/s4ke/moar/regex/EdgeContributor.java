package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.Set;

import com.github.s4ke.moar.moa.EdgeGraph;
import com.github.s4ke.moar.moa.State;
import com.github.s4ke.moar.moa.Variable;

/**
 * @author Martin Braun
 */
interface EdgeContributor {

	void contributeEdges(
			EdgeGraph edgeGraph, Map<String, Variable> variables, Set<State> states,
			Map<Regex, Map<String, State>> selfRelevant);

}
