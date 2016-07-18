package com.github.s4ke.moar.util;

import java.util.Stack;
import java.util.function.Function;

/**
 * @author Martin Braun
 */
public class ExecStack<X> {

	private Stack<Function<X, X>> stack = new Stack<>();

	public void add(Function<X, X> supplier) {
		this.stack.add( supplier );
	}

	public X exec(X start) {
		X res = start;
		while(this.stack.size() > 0) {
			Function<X, X> cur = this.stack.pop();
			res = cur.apply( res );
		}
		return res;
	}

}