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
