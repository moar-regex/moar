package com.github.s4ke.moar.lucene.query;

import java.io.IOException;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ConstantScoreScorer;
import org.apache.lucene.search.ConstantScoreWeight;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.BytesRef;

/**
 * @author Martin Braun
 */
public class MoarQuery extends Query {

	private final String field;
	private final MoaPattern moaPattern;

	public MoarQuery(String field, MoaPattern moaPattern) {
		this.field = field;
		this.moaPattern = moaPattern;
	}


	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores) {
		return new MoarQueryWeight( this );
	}

	private class MoarQueryWeight extends ConstantScoreWeight {

		protected MoarQueryWeight(Query query) {
			super( query );
		}

		@Override
		public Scorer scorer(LeafReaderContext context) throws IOException {
			return new ConstantScoreScorer( this, score(), new MoarQueryDocIdSetIterator( context.reader() ) );
		}

		private class MoarQueryDocIdSetIterator extends DocIdSetIterator {

			private boolean exhausted = false;

			private int docId = -1;
			private final IndexReader indexReader;

			private MoarQueryDocIdSetIterator(IndexReader indexReader) {
				this.indexReader = indexReader;
			}

			@Override
			public int docID() {
				return exhausted ? NO_MORE_DOCS : this.docId;
			}

			@Override
			public int nextDoc() throws IOException {
				MoaMatcher matcher = MoarQuery.this.moaPattern.matcher( "" );
				int docFound = -1;
				outer:
				for ( int i = this.docId + 1; i < this.indexReader.maxDoc(); ++i ) {
					TermsEnum termsEnum = this.indexReader.getTermVector( i, MoarQuery.this.field ).iterator();
					BytesRef bytesRef;
					while ( (bytesRef = termsEnum.next()) != null ) {
						//FIXME: UTF-16/UTF-32
						String str = bytesRef.utf8ToString();
						if ( matcher.reuse( str ).matches() ) {
							docFound = i;
							break outer;
						}
					}

				}
				if ( docFound == -1 ) {
					return NO_MORE_DOCS;
				}
				this.docId = docFound;
				return this.docId;
			}

			@Override
			public int advance(int target) throws IOException {
				int doc;
				while ( (doc = nextDoc()) < target ) {
				}
				return doc;
			}

			@Override
			public long cost() {
				//FIXME:
				return Integer.MAX_VALUE;
			}
		}
	}

	@Override
	public String toString(String s) {
		return this.moaPattern.toString();
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
