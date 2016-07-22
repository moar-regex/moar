package com.github.s4ke.moar.json;

import com.github.s4ke.moar.MoaPattern;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class MoarJSONSerializerTest {

	@Test
	public void testJSONSerialization() {
		MoaPattern pattern = MoaPattern.compile( "^(?<toast>[a-z]b[^b]\\w)\\k<toast>$" );
		String jsonString = MoarJSONSerializer.toJSON( pattern );
		System.out.println( jsonString );
		assertTrue( pattern.matcher( "abcdabcd" ).matches() );

		MoaPattern fromJSON = MoarJSONSerializer.fromJSON( jsonString );
		System.out.println( "\n" + MoarJSONSerializer.toJSON( fromJSON ) );
		assertTrue( pattern.matcher( "abcdabcd" ).matches() );
	}

}
