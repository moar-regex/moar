package com.github.s4ke.moar.json;

import com.github.s4ke.moar.MoaPattern;

import org.junit.Test;

/**
 * @author Martin Braun
 */
public class MoarJSONSerializerTest {

	@Test
	public void testJSONSerialization() {
		MoaPattern pattern = MoaPattern.compile( "^(?<toast>[a-z]b[^b])\\k<toast>$" );
		String jsonString = MoarJSONSerializer.toJSON( pattern );
		System.out.println( jsonString );
	}

}
