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
package com.github.s4ke.moar.lucene.query.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.lucene.query.MoarQuery;
import com.github.s4ke.moar.util.Perf;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Braun
 */
public class MoarQueryPerfTest extends BaseLuceneTest {

	@Before
	public void setup() throws IOException {
		this.setup(FSDirectory.open( Paths.get( "lucene_dir", "moarquery_perf" ) ), new Random(1231233471));
	}

	private void setupComparisonData() throws IOException {
		this.clearIndex();

		System.out.println( "writing into index" );
		try (IndexWriter iw = new IndexWriter( this.d, this.getIwc() )) {

			for ( int i = 0; i < 1000; ++i ) {
				Document doc = createDocument();
				Field idField = new Field( "id", String.valueOf( i ), ID_FIELD_TYPE );
				Field field = new Field( "tag", randomString( WORD_COUNT_PER_DOCUMENT ), TAGS_FIELD_TYPE );
				doc.add( field );
				doc.add( idField );
				iw.addDocument( doc );
				if ( i % 100 == 0 ) {
					System.out.println( i );
				}

			}
			iw.commit();
		}
		System.out.println( "finished setting up index data" );
	}

	@Test
	public void testComparison() throws IOException {
		this.setupComparisonData();

		try (IndexReader ir = DirectoryReader.open( d )) {
			IndexSearcher is = new IndexSearcher( ir );
			Perf perf = new Perf( true );

			for ( int i = 0; i < 1000; ++i ) {
				String wordOfChoice = WORDS.get( this.random.nextInt( WORDS.size() ) );
				wordOfChoice = wordOfChoice.substring( 0, this.random.nextInt( wordOfChoice.length() - 1 ) + 1 );
				wordOfChoice += ".*";
				System.out.println( wordOfChoice );
				{
					perf.pre();
					MoaPattern pattern = MoaPattern.compile( wordOfChoice );
					MoarQuery tq = new MoarQuery( "tag", pattern );

					TopDocs td = is.search( tq, 10 );
					System.out.println( td.totalHits + " moar query hits" );
					perf.after();
					perf.report( "searching with moar" );
				}

				{
					RegexpQuery regexpQuery = new RegexpQuery( new Term( "tag", wordOfChoice ) );
					perf.pre();
					TopDocs td = is.search(
							regexpQuery
							, 10
					);
					System.out.println( td.totalHits + " regexp query hits" );
					perf.after();
					perf.report( "searching with regexp" );
				}
			}
		}
	}

	@After
	public void tearDown() throws IOException {
		this.d.close();
	}
}
