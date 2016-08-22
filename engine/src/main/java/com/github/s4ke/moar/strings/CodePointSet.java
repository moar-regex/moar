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
package com.github.s4ke.moar.strings;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import com.github.s4ke.moar.util.RangeRep;
import com.google.common.collect.TreeRangeSet;

/**
 * @author Martin Braun
 */
public class CodePointSet {

	private final Set<Integer> set;
	private final RangeRep range;

	private CodePointSet(Set<Integer> set, RangeRep ranges) {
		this.set = set;
		this.range = ranges;
	}

	public static CodePointSet range(RangeRep ranges) {
		return new CodePointSet( null, ranges );
	}

	public static CodePointSet range(RangeRep... ranges) {
		TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
		for ( RangeRep rangeRep : ranges ) {
			rangeSet.addAll( rangeRep.getRangeSet() );
		}
		return new CodePointSet( null, RangeRep.of( rangeSet ) );
	}

	public static CodePointSet set(Set<Integer> set) {
		return new CodePointSet( set, null );
	}

	public CodePointSet negative() {
		RangeRep negativeRange = null;
		if ( this.range != null ) {
			negativeRange = this.range.negative();
		}
		Set<Integer> negativeSet = null;
		if ( this.set != null ) {
			negativeSet = new AbstractSet<Integer>() {
				@Override
				public Iterator<Integer> iterator() {
					throw new UnsupportedOperationException();
				}

				@Override
				public int size() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean contains(Object o) {
					return !CodePointSet.this.set.contains( o );
				}
			};
		}
		return new CodePointSet( negativeSet, negativeRange );
	}

	public boolean intersects(int codePoint) {
		if ( this.range != null ) {
			return this.range.intersects( codePoint );
		}
		else if ( this.set != null ) {
			return this.set.contains( codePoint );
		}
		else {
			throw new AssertionError();
		}
	}

	public boolean intersects(CodePointSet other) {
		if ( this.set != null ) {
			if ( other.set != null ) {
				return intersects( this.set, other.set );
			}
			else if ( other.range != null ) {
				for ( Integer val : this.set ) {
					if ( other.intersects( val ) ) {
						return true;
					}
				}
				return false;
			}
			else {
				throw new AssertionError( "wtf" );
			}
		}
		else if ( this.range != null ) {
			if ( other.set != null ) {
				for ( Integer val : other.set ) {
					if ( this.intersects( val ) ) {
						return true;
					}
				}
				return false;
			}
			else if ( other.range != null ) {
				return this.range.intersects( other.range );
			}
			else {
				throw new AssertionError( "wtf" );
			}
		}
		else {
			throw new AssertionError( "wtf" );
		}
	}

	private static boolean intersects(Set<Integer> fst, Set<Integer> snd) {
		for ( Integer f : fst ) {
			if ( snd.contains( f ) ) {
				return true;
			}
		}
		for ( Integer s : snd ) {
			if ( fst.contains( s ) ) {
				return true;
			}
		}
		return false;
	}


}
