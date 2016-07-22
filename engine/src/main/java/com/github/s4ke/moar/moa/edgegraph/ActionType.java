package com.github.s4ke.moar.moa.edgegraph;

import com.github.s4ke.moar.moa.states.Variable;

/**
 * @author Martin Braun
 */
public enum ActionType {
	OPEN {
		@Override
		public void act(String variableName, Variable val) {
			if ( !val.isOpen() ) {
				val.open();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "o(%s)", variableName );
		}
	},
	CLOSE {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.close();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "c(%s)", variableName );
		}
	},
	RESET {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.contents.reset();
			}
		}

		@Override
		public String toString(String variableName) {
			return String.format( "r(%s)", variableName );
		}
	};

	public static ActionType fromString(String str) {
		switch ( str ) {
			case "o":
				return OPEN;
			case "c":
				return CLOSE;
			case "r":
				return RESET;
			default:
				throw new IllegalArgumentException( "unrecognized ActionType String representation: " + str );
		}
	}

	public abstract void act(String variableName, Variable val);

	public abstract String toString(String variableName);
}
