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
package com.github.s4ke.moar.lucene.query;

import java.io.IOException;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.util.CharSeq;
import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

/**
 * @author Martin Braun
 */
public class MoarQuery extends MultiTermQuery {

	private final MoaPattern moaPattern;

	public MoarQuery(String field, MoaPattern moaPattern) {
		super( field );
		this.moaPattern = moaPattern;
	}


	@Override
	public String toString(String s) {
		return this.moaPattern.toString();
	}


	@Override
	protected TermsEnum getTermsEnum(
			Terms terms, AttributeSource atts) throws IOException {
		MoaMatcher matcher = this.moaPattern.matcher( "" );
		TermsEnum termsEnum = terms.iterator();
		return new MoarTermsEnum( matcher, termsEnum );
	}

	private static class MoarTermsEnum extends FilteredTermsEnum {

		private final MoaMatcher matcher;

		private MoarTermsEnum(MoaMatcher matcher, TermsEnum termsEnum) throws IOException {
			super( termsEnum );
			this.matcher = matcher;
			this.setInitialSeekTerm( termsEnum.next() );
		}

		@Override
		protected AcceptStatus accept(BytesRef term) throws IOException {
			CharSeq byteCharSeq = new ByteCharSeq( term );
			if ( matcher.reuse( byteCharSeq ).matches() ) {
				return AcceptStatus.YES;
			}
			return AcceptStatus.NO;
		}
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		MoarQuery moarQuery = (MoarQuery) o;

		return !(moaPattern != null ? !moaPattern.equals( moarQuery.moaPattern ) : moarQuery.moaPattern != null);
	}

	@Override
	public int hashCode() {
		int result = 0;
		result = 31 * result + (moaPattern != null ? moaPattern.hashCode() : 0);
		return result;
	}

}
