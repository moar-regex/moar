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
