package com.github.s4ke.moar.regex;

import java.util.Map;
import java.util.function.Supplier;

import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
interface VariableOccurence {

	void calculateVariableOccurences(Map<String, Variable> variables, Supplier<Integer> varIdxSupplier);

}
