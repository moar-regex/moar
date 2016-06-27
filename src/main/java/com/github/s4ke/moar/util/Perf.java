package com.github.s4ke.moar.util;

/**
 * @author Martin Braun
 */
public class Perf {

	private final boolean report;

	private long pre;
	private long after;

	public Perf(boolean report) {
		this.report = report;
	}

	public void pre() {
		this.pre = System.nanoTime();
	}

	public void after() {
		this.after = System.nanoTime();
	}

	public long diff() {
		return this.after - this.pre;
	}

	public void report(String name) {
		if ( this.report ) {
			System.out.println( name + " took " + this.diff() + "ns" );
		}
	}

}
