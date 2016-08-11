package com.github.s4ke.moar.lucene.query.test;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Martin Braun
 */
public class MoarQueryTest {

	private static final Version VERSION = Version.LUCENE_6_1_0;

	private Directory d;

	@Before
	public void setup() throws IOException {
		this.d = new RAMDirectory();
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
		IndexWriter iw = new IndexWriter( this.d, iwc );
		try {
			Document doc = new Document();
			FieldType tagsFieldType = new FieldType();
			tagsFieldType.setStored( true );
			tagsFieldType.setTokenized( true );
			tagsFieldType.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
			FieldType idFieldType = new FieldType();
			idFieldType.setStored( true );
			idFieldType.setTokenized( false );
			idFieldType.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );

			Field idField = new Field( "id", "1", idFieldType );
			Field field = new Field( "tag", "star-trek captain-picard tv-show space", tagsFieldType );
			doc.add( field );
			doc.add( idField );
			iw.addDocument( doc );

			field = new Field( "tag", "star-wars darth-vader space", tagsFieldType );
			idField = new Field( "id", "2", idFieldType );
			doc.add( field );
			doc.add( idField );
			iw.addDocument( doc );

			iw.commit();
		}
		finally {
			iw.close();
		}
	}

	@Test
	public void testBasics() throws IOException {
		IndexReader ir = DirectoryReader.open( d );
		try {
			IndexSearcher is = new IndexSearcher( ir );

			Term termToFind = new Term( "tag", "space" );
			TermQuery tq = new TermQuery( termToFind );

			TopDocs td = is.search( tq, 10 );
			assertEquals( 2, td.scoreDocs.length );
		}
		finally {
			ir.close();
		}
	}

	@After
	public void tearDown() throws IOException {
		this.d.close();
	}
}
