/*
 The MIT License (MIT)

 Copyright (c) 2016 Martin Braun

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.github.s4ke.moar.util;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * @author Martin Braun
 */
public class RangeRep {

	private final RangeSet<Integer> rangeSet;

	private RangeRep(int from, int to) {
		this.rangeSet = TreeRangeSet.create();
		this.rangeSet.add( Range.closed( from, to ) );
	}

	private RangeRep(RangeSet<Integer> rangeSet) {
		this.rangeSet = rangeSet;
	}

	public static RangeRep of(RangeSet<Integer> rangeSet) {
		return new RangeRep( rangeSet );
	}

	public static RangeRep of(int from, int to) {
		return new RangeRep( from, to );
	}

	public RangeRep negative() {
		return of( this.rangeSet.complement() );
	}

	public RangeSet<Integer> getRangeSet() {
		return this.rangeSet;
	}

	public boolean intersects(RangeRep range) {
		return !intersect( this.rangeSet, range.rangeSet ).isEmpty();
	}

	public boolean intersects(int value) {
		return this.rangeSet.contains( value );
	}

	public StringBuilder append(StringBuilder builder) {
		//this is only ever called for things in the normal char range so we dont
		//have to check whether the the range is valid
		for ( Range range : this.rangeSet.asRanges() ) {
			builder = builder.appendCodePoint( (Integer) range.lowerEndpoint() ).append( "-" ).appendCodePoint(
					(Integer) range.upperEndpoint()
			);
		}
		return builder;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		RangeRep rangeRep = (RangeRep) o;

		return !(rangeSet != null ? !rangeSet.equals( rangeRep.rangeSet ) : rangeRep.rangeSet != null);

	}

	@Override
	public int hashCode() {
		return rangeSet != null ? rangeSet.hashCode() : 0;
	}

	@Override
	public String toString() {
		return this.append( new StringBuilder() ).toString();
	}

	static <T extends Comparable> RangeSet<T> intersect(RangeSet<T> a, RangeSet<T> b) {
		RangeSet<T> copy = TreeRangeSet.create( a );
		copy.removeAll( b.complement() );
		return copy;
	}

}
