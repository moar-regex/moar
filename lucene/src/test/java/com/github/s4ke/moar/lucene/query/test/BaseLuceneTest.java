
package com.github.s4ke.moar.lucene.query.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * @author Martin Braun
 */
public class BaseLuceneTest {

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

}
