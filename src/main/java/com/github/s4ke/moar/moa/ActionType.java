package com.github.s4ke.moar.moa;

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
	},
	CLOSE {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.close();
			}

		}
	},
	RESET {
		@Override
		public void act(String variableName, Variable val) {
			if ( val.isOpen() ) {
				val.contents.reset();
			}

		}
	};

	public abstract void act(String variableName, Variable val);
}
