package com.github.s4ke.moar.lucene.query.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

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
