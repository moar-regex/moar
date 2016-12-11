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

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.lucene.query.MoarQuery;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.FSDirectory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Braun
 */
public class MoarQueryTest extends BaseLuceneTest {

	private static final String UNIQUE = "unique";

	@Before
	public void setup() throws IOException {
		this.setup( FSDirectory.open( Paths.get( "lucene_dir", "moarquery" ) ), new Random( 123273472 ) );
	}

	private void setupData() throws IOException {
		System.out.println( "clearing index" );
		{
			try (IndexWriter iw = new IndexWriter( this.d, this.getIwc() )) {
				iw.deleteAll();
			}
		}

		System.out.println( "writing into index" );
		try (IndexWriter iw = new IndexWriter( this.d, this.getIwc() )) {


			{
				Document doc = createDocument();
				Field idField = new Field( "id", String.valueOf( -1 ), ID_FIELD_TYPE );
				Field field = new Field( "tag", UNIQUE, TAGS_FIELD_TYPE );
				doc.add( field );
				doc.add( idField );
				iw.addDocument( doc );
			}

			for ( int i = 0; i < 100; ++i ) {
				Document doc = createDocument();
				Field idField = new Field( "id", String.valueOf( i ), ID_FIELD_TYPE );
				Field field = new Field( "tag", randomString( WORD_COUNT_PER_DOCUMENT ), TAGS_FIELD_TYPE );
				doc.add( field );
				doc.add( idField );
				iw.addDocument( doc );
				if ( i % 10 == 0 ) {
					System.out.println( i );
				}

			}
			iw.commit();
		}
		System.out.println( "finished setting up index data" );
	}

	@Test
	public void testBasics() throws IOException {
		this.setupData();
		MoaPattern pattern = MoaPattern.compile( UNIQUE );
		MoarQuery tq = new MoarQuery( "tag", pattern );
		this.assertHits( tq, 1 );
	}

	@Test
	public void testBackRef() throws IOException {
		this.setupBackRefData();
		{
			//add one document that should not match
			Document doc = createDocument();
			doc.add( new Field( "id", String.valueOf( -1 ), ID_FIELD_TYPE ) );
			doc.add( new Field( "tag", UNIQUE, TAGS_FIELD_TYPE ) );
			this.writeSingleDoc( doc );
		}

		MoaPattern pattern = MoaPattern.compile( "(a*)b\\1" );

		MoarQuery tq = new MoarQuery( "tag", pattern );
		this.assertHits( new MatchAllDocsQuery(), BACK_REF_DOC_COUNT + 1 );
		this.assertHits( tq, BACK_REF_DOC_COUNT );
	}

	@After
	public void tearDown() throws IOException {
		this.d.close();
	}
}
