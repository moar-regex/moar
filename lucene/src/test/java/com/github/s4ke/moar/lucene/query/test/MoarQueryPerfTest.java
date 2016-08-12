package com.github.s4ke.moar.lucene.query.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.lucene.query.MoarQuery;
import com.github.s4ke.moar.util.Perf;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Braun
 */
public class MoarQueryPerfTest extends BaseLuceneTest {

	private static final Version VERSION = Version.LUCENE_6_1_0;

	private static final List<String> WORDS = Arrays.asList(
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

	private static Random RANDOM;
	private static final int WORD_COUNT_PER_DOCUMENT = 50;

	private static String randomString(int words) {
		StringBuilder ret = new StringBuilder();
		for ( int i = 0; i < words; ++i ) {
			ret.append( WORDS.get( RANDOM.nextInt( WORDS.size() ) ) ).append( " " );
		}
		return ret.toString();
	}

	private Directory d;

	@Before
	public void setup() throws IOException {
		RANDOM = new Random( 16812875 );
		this.d = FSDirectory.open( Paths.get( "lucene_dir", "moarquery_perf" ) );
	}

	private void clearIndex() throws IOException {
		System.out.println( "clearing index" );
		{
			WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

			IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
			try (IndexWriter iw = new IndexWriter( this.d, iwc )) {
				iw.deleteAll();
			}
		}
	}

	private void setupComparisonData() throws IOException {
		this.clearIndex();

		System.out.println( "writing into index" );
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
		try (IndexWriter iw = new IndexWriter( this.d, iwc )) {

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

	private String repeat(String str, int count) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < count; ++i ) {
			builder.append( str );
		}
		return builder.toString();
	}

	private void setupBackRefData() throws IOException {
		this.clearIndex();

		System.out.println( "writing into index" );
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig( analyzer );
		try (IndexWriter iw = new IndexWriter( this.d, iwc )) {

			for ( int i = 0; i < 1000; ++i ) {
				Document doc = createDocument();
				int repeatCount = RANDOM.nextInt( 100 ) + 1;
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

	@Test
	public void testBackRefData() throws IOException {
		this.setupBackRefData();


		try (IndexReader ir = DirectoryReader.open( d )) {

			IndexSearcher is = new IndexSearcher( ir );
			Perf perf = new Perf( true );
			perf.pre();
			MoaPattern pattern = MoaPattern.compile( "(a*)b\\1" );
			MoarQuery tq = new MoarQuery( "tag", pattern );
			TopDocs td = is.search( tq, 10 );
			System.out.println( td.totalHits + " moar query hits" );
			perf.after();
			perf.report( "backref time" );
		}

	}

	@Test
	public void testComparison() throws IOException {
		this.setupComparisonData();

		try (IndexReader ir = DirectoryReader.open( d )) {
			IndexSearcher is = new IndexSearcher( ir );
			Perf perf = new Perf( true );

			for ( int i = 0; i < 1000; ++i ) {
				String wordOfChoice = WORDS.get( RANDOM.nextInt( WORDS.size() ) );
				wordOfChoice = wordOfChoice.substring( 0, RANDOM.nextInt( wordOfChoice.length() - 1 ) + 1 );
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
