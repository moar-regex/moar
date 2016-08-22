package com.github.s4ke.moar.lucene.query;

import com.github.s4ke.moar.util.CharSeq;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.UnicodeUtil;

/**
 * @author Martin Braun
 */
public class ByteCharSeq implements CharSeq {

	private final BytesRef contents;
	private final byte[] tmpByte = new byte[1];
	private final char[] tmpChar = new char[1];

	public ByteCharSeq(BytesRef contents) {
		this.contents = contents;
	}

	@Override
	public int codePointLength() {
		return this.contents.length;
	}

	@Override
	public int codePoint(int index) {
		//FIXME: is this the correct behaviour?
		this.tmpByte[0] = this.contents.bytes[index];
		UnicodeUtil.UTF8toUTF16( this.tmpByte, 0, 1, this.tmpChar );
		return this.tmpChar[0] & 0xFFFF;
	}

	@Override
	public String subSequence(int start, int end) {
		return new String( this.contents.bytes, start, end );
	}

	@Override
	public String toString() {
		return new String( this.contents.bytes, 0, this.contents.length );
	}
}
