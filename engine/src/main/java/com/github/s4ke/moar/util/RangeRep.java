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
