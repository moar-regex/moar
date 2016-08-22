
package com.github.s4ke.moar.lucene.query.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import static org.junit.Assert.assertEquals;

/**
 * @author Martin Braun
 */
public class BaseLuceneTest {

	public static final List<String> WORDS = Arrays.asList(
			"toast",
			"marmalade",
			"peanutbutter",
			"jelly",
			"moar",
			"lucene",
			"regex",
			"hello",
			"bye",
			"bread",
			"baguette",
			"pizza",
			"kebap",
			"chili",
			"pepperoni",
			"space"
	);

	public static final int WORD_COUNT_PER_DOCUMENT = 50;
	public static final int BACK_REF_DOC_COUNT = 1000;

	protected Directory d;

	public Document createDocument() {
		return new Document();
	}

	public static final FieldType ID_FIELD_TYPE;

	static {
		FieldType idFieldType = new FieldType();
		idFieldType.setStored( true );
		idFieldType.setTokenized( false );
		idFieldType.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
		ID_FIELD_TYPE = idFieldType;
	}

	public static final FieldType TAGS_FIELD_TYPE;

	static {
		FieldType tagsFieldType = new FieldType();
		tagsFieldType.setStored( false );
		tagsFieldType.setTokenized( true );
		tagsFieldType.setStoreTermVectors( false );
		tagsFieldType.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
		TAGS_FIELD_TYPE = tagsFieldType;
	}

	protected Random random;

	public void setup(Directory directory, Random random) {
		this.d = directory;
		if ( random == null ) {
			this.random = new Random();
		}
		else {
			this.random = random;
		}
	}

	public void writeSingleDoc(Document document) throws IOException {
		try (IndexWriter iw = new IndexWriter( this.d, this.getIwc() )) {
			iw.addDocument( document );
			iw.commit();
		}
	}

	public String randomString(int words) {
		StringBuilder ret = new StringBuilder();
		for ( int i = 0; i < words; ++i ) {
			ret.append( WORDS.get( this.random.nextInt( WORDS.size() ) ) ).append( " " );
		}
		return ret.toString();
	}

	public String repeat(String str, int count) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < count; ++i ) {
			builder.append( str );
		}
		return builder.toString();
	}


	public void clearIndex() throws IOException {
		System.out.println( "clearing index" );
		{
			WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

			IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
			try (IndexWriter iw = new IndexWriter( this.d, iwc )) {
				iw.deleteAll();
			}
		}
	}

	public void assertHits(Query query, int hitCount) throws IOException {
		try (IndexReader ir = DirectoryReader.open( d )) {
			IndexSearcher searcher = new IndexSearcher( ir );
			TopDocs td = searcher.search( query, 10 );
			assertEquals( "hitCount didn't match expected hit count", hitCount, td.totalHits );
		}
	}

	public void setupBackRefData() throws IOException {
		this.clearIndex();

		System.out.println( "writing into index" );
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
		try (IndexWriter iw = new IndexWriter( this.d, iwc )) {

			for ( int i = 0; i < BACK_REF_DOC_COUNT; ++i ) {
				Document doc = createDocument();
				int repeatCount = random.nextInt( 100 ) + 1;
				Field idField = new Field( "id", String.valueOf( i ), ID_FIELD_TYPE );
				Field field = new Field(
						"tag", randomString( WORD_COUNT_PER_DOCUMENT ) + " " + repeat(
						"a",
						repeatCount
				) + "b" + repeat( "a", repeatCount ), TAGS_FIELD_TYPE
				);
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

	public IndexWriterConfig getIwc() {
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
		return new IndexWriterConfig( analyzer );
	}
}
