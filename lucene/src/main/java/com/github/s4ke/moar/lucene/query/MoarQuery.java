package com.github.s4ke.moar.lucene.query;

import java.io.IOException;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.regex.CharacterClassesUtils;
import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.UnicodeUtil;

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
		return null;
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	protected TermsEnum getTermsEnum(
			Terms terms, AttributeSource atts) throws IOException {
		return null;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	private static final class MoarTermEnum extends FilteredTermsEnum {

		public MoarTermEnum(TermsEnum tenum) {
			super( tenum );
		}

		@Override
		protected AcceptStatus accept(BytesRef term) throws IOException {

			return AcceptStatus.NO;
		}
	}

}
