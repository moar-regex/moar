package com.github.s4ke.moar.regex;

import java.util.regex.Pattern;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.util.GenericMatcher;
import com.github.s4ke.moar.util.MoaMatcher;
import com.github.s4ke.moar.util.PatternMatcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class VSJavaPattern {

	@Test
	public void testSimple() {
		time( gen( Regex.str( "a" ) ), "a", true );
		time( gen( Pattern.compile( "a" ) ), "a", true );
		System.out.println();

		time( gen( Regex.str( "test" ) ), "test", true );
		time( gen( Pattern.compile( "test" ) ), "test", true );
		System.out.println( "---------------------" );
	}

	@Test
	public void testBackRef() {
		String testStr = "aaaaaaaaaaaaaaaaaaa|aaaaaaaaaaaaaaaaaaa";

		time(
				gen(
						Regex.str( "a" )
								.plus()
								.bind( "x" )
								.and( "|" )
								.and( Regex.reference( "x" ) )
				), testStr
				, true
		);
		time(
				gen(
						Pattern.compile( "(a+)\\|\\1" )
				), testStr, true
		);
	}

	private GenericMatcher gen(Object obj) {
		if ( obj instanceof Moa ) {
			return new MoaMatcher( (Moa) obj );
		}
		if ( obj instanceof Regex ) {
			return new MoaMatcher( (Regex) obj );
		}
		if ( obj instanceof Pattern ) {
			return new PatternMatcher( (Pattern) obj );
		}
		return null;
	}

	public void time(GenericMatcher matcher, String string, boolean expectedResult) {
		for ( int i = 0; i < 1000000; ++i ) {
			assertEquals( expectedResult, matcher.check( string ) );
		}
		//warm up
		long totalDiff = 0;
		for ( int i = 0; i < 100000; ++i ) {
			long pre = System.nanoTime();
			assertEquals( expectedResult, matcher.check( string ) );
			long after = System.nanoTime();
			long diff = after - pre;
			totalDiff += diff;
		}
		System.out.println( matcher + " took " + totalDiff / 100000 + "ns" );
	}

}
