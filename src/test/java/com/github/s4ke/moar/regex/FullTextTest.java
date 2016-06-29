package com.github.s4ke.moar.regex;

import com.github.s4ke.moar.moa.MoaMatcher;
import com.github.s4ke.moar.moa.Moa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class FullTextTest {

	@Test
	public void testMultiLine() {
		{
			Regex regex = Regex.str( "toast" ).or( "or is it?" ).bind( "x" );
			Moa moa = regex.toMoa();
			MoaMatcher matcher = moa.matcher( "toast is not a beverage\nno wait, or is it?\nb" );
			int matchCount = 0;
			while ( matcher.nextMatch() ) {
				++matchCount;
				assertTrue( moa.matcher( matcher.getVariableContent( 1 ) ).checkAsSingleWord() );
			}
			assertEquals( 2, matchCount );
		}
	}

	@Test
	public void testCoolLanguage() {
		Regex regex = Regex.reference( "x" )
				.bind( "y" )
				.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
				.plus().bind( "all" );
		System.out.println( regex.toString() );
		Moa moa = regex.toMoa();
		{
			MoaMatcher matcher = moa.matcher( "aaaa" );
			assertTrue( matcher.nextMatch() );
			assertEquals( "aaaa", matcher.getVariableContent( "all" ) );
		}
		{
			MoaMatcher matcher = moa.matcher( "aaaaa" );
			assertFalse( moa.check( "aaaaa" ) );
			assertTrue( matcher.nextMatch() );
			assertEquals( "aaaa", matcher.getVariableContent( "all" ) );
		}
	}

}
