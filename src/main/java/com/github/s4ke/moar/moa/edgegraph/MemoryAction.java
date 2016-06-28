/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.s4ke.moar.moa.edgegraph;

import java.util.Map;

import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
public class MemoryAction {

	public static final MemoryAction NO_OP = null;

	public final ActionType actionType;
	public final String variable;

	public MemoryAction(ActionType actionType, String variable) {
		this.actionType = actionType;
		this.variable = variable;
	}

	public void act(Map<String, Variable> variables) {
		Variable val = variables.get( this.variable );
		if ( val == null ) {
			throw new AssertionError( "variable with name " + this.variable + " not found" );
		}
		this.actionType.act( this.variable, val );
	}

	@Override
	public String toString() {
		return "var=" + this.variable +
				"action=" + this.actionType;
	}
}
