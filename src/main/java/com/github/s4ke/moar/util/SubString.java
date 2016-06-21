package com.github.s4ke.moar.util;

/**
 * <p>
 * A CharSequence implementation that allows us to
 * create SubSequences of CharSequences without
 * having to build a string.
 * <p/>
 * <p>
 * This helps to reduce the amount of time spent
 * building String objects and also reduces the amount
 * of memory used compared to basic Strings.
 * </p>
 *
 * @author Martin Braun
 */
public class SubString implements CharSequence {

	private CharSequence underlying;
	private int start;
	private int end;

	public SubString() {
		this.underlying = null;
		this.start = 0;
		this.end = 0;
	}

	public SubString(CharSequence underlying, int start, int end) {
		this.underlying = underlying;
		this.start = start;
		this.end = end;
	}

	public void update(SubString underlying, int start, int end) {
		this.underlying = underlying.underlying;
		this.start = start;
		this.end = end;
	}

	public void appendOrOverwrite(SubString substring) {
		if ( this.underlying == null ) {
			this.update( substring, substring.start, substring.end );
		}
		else {
			if ( this.end != substring.start || this.underlying != substring.underlying ) {
				this.update( substring, substring.start, substring.end );
			}
			this.end = substring.end;
		}
	}

	public void update(CharSequence underlying, int start, int end) {
		if ( underlying instanceof SubString ) {
			this.update( (SubString) underlying, start, end );
		}
		this.underlying = underlying;
		this.start = start;
		this.end = end;
	}

	public void reset() {
		this.underlying = null;
		this.start = 0;
		this.end = 0;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SubString subString = (SubString) o;

		if ( start != subString.start ) {
			return false;
		}
		if ( end != subString.end ) {
			return false;
		}

		return !(underlying != null ? !underlying.equals( subString.underlying ) : subString.underlying != null);
	}

	@Override
	public int hashCode() {
		int result = underlying != null ? underlying.hashCode() : 0;
		result = 31 * result + start;
		result = 31 * result + end;
		return result;
	}

	@Override
	public int length() {
		return this.end - start;
	}

	@Override
	public char charAt(int index) {
		if ( this.underlying == null ) {
			throw new IndexOutOfBoundsException();
		}
		return this.underlying.charAt( this.start + index );
	}

	@Override
	public SubString subSequence(int start, int end) {
		if ( end - start > this.length() ) {
			throw new IndexOutOfBoundsException();
		}
		return new SubString( this.underlying, this.start + start, this.end + end );
	}

	@Override
	public String toString() {
		if ( this.underlying == null ) {
			return "";
		}
		return this.underlying.subSequence( this.start, this.end ).toString();
	}

}
