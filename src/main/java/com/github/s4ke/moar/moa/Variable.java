package com.github.s4ke.moar.moa;

import com.github.s4ke.moar.util.SubString;

/**
 * @author Martin Braun
 */
public class Variable {

	public final SubString contents = new SubString();
	public final String name;
	private boolean open = false;
	private int occurenceInRegex = -1;

	public Variable(String name) {
		this.name = name;
	}

	public void open() {
		this.open = true;
	}

	public boolean isOpen() {
		return this.open;
	}

	public void close() {
		this.open = false;
	}

	public int getOccurenceInRegex() {
		return this.occurenceInRegex;
	}

	public void setOccurenceInRegex(int occurenceInRegex) {
		this.occurenceInRegex = occurenceInRegex;
	}

	@Override
	public String toString() {
		return "Variable{" +
				"contents=" + contents +
				", name='" + name + '\'' +
				", open=" + open +
				'}';
	}

	public String getEdgeString() {
		return this.contents.toString();
	}

	public String getContents() {
		return this.contents.toString();
	}

	public void reset() {
		this.contents.reset();
	}

	public boolean canConsume() {
		return this.isOpen();
	}

	public void consume(SubString str) {
		if ( !this.canConsume() ) {
			throw new IllegalStateException( "cannot consume at the moment!" );
		}
		this.contents.appendOrOverwrite( str );
	}

}
