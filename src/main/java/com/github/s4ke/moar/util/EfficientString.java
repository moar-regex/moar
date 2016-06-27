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
public class EfficientString implements CharSequence {

	private String underlying;
	private int start;
	private int end;

	public EfficientString() {
		this.underlying = null;
		this.start = 0;
		this.end = 0;
	}

	public EfficientString(String underlying, int start, int end) {
		this.underlying = underlying;
		this.start = start;
		this.end = end;
	}

	public EfficientString(String underlying) {
		this.underlying = underlying;
		this.start = 0;
		this.end = underlying.length();
	}

	public void update(EfficientString underlying, int start, int end) {
		this.underlying = underlying.underlying;
		this.start = start;
		this.end = end;
	}

	public void appendOrOverwrite(EfficientString substring) {
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

	public void update(String underlying, int start, int end) {
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

		EfficientString efficientString = (EfficientString) o;

		if ( this.length() != efficientString.length() ) {
			return false;
		}

		return this.equalTo( efficientString );
	}

	public boolean equalTo(CharSequence str) {
		int ownLength = this.length();
		if ( ownLength != str.length() ) {
			return false;
		}
		for ( int i = 0; i < ownLength; ++i ) {
			if ( this.charAt( i ) != str.charAt( i ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for ( int i = 0; i < this.length(); ++i ) {
			result = 31 * result + Character.hashCode( this.charAt( i ) );
		}
		result = 31 * result + Integer.hashCode( this.length() );
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
	public EfficientString subSequence(int start, int end) {
		if ( end - start > this.length() ) {
			throw new IndexOutOfBoundsException();
		}
		return new EfficientString( this.underlying, this.start + start, this.end + end );
	}

	@Override
	public String toString() {
		if ( this.underlying == null ) {
			return "";
		}
		if ( this.length() == this.underlying.length() ) {
			return this.underlying.toString();
		}
		return this.underlying.subSequence( this.start, this.end ).toString();
	}

}
