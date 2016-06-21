package com.github.s4ke.moar.regex;

import java.util.HashMap;

import com.github.s4ke.moar.moa.Moa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Braun
 */
public class MoaWithDSLTest {

	@Test
	public void testSimple() {
		Regex regex = Regex.str( "a" ).build();
		assertTrue( regex.toMoa().check( "a" ) );
	}

	@Test
	public void testLongerString() {
		Moa moa = Regex.str( "test" ).build().toMoa();
		assertTrue( moa.check( "test" ) );
	}

	@Test
	public void testOr() {
		Regex regex = Regex.str( "a" ).or( "b" ).build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "b" ) );
		assertFalse( moa.check( "" ) );
		assertFalse( moa.check( "Z" ) );
	}

	@Test
	public void testOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "b" ) );
	}

	@Test
	public void testBiggerOrEpsilon() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).and( Regex.str( "b" ) ).build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "ab" ) );
		assertTrue( moa.check( "b" ) );
		assertFalse( moa.check( "" ) );
	}

	@Test
	public void testAnd() {
		Moa moa = Regex.str( "a" ).and( "b" ).build().toMoa();
		assertTrue( moa.check( "ab" ) );
		assertFalse( moa.check( "a" ) );
		assertFalse( moa.check( "" ) );
		assertFalse( moa.check( "ba" ) );
	}

	@Test
	public void testSimpleBinding() {
		Regex regex = Regex.str( "a" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) ).build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a|a" ) );
		assertEquals( "a", moa.getVariableContent( "toast", 0 ) );
		assertFalse( moa.check( "a|aa" ) );
	}

	@Test
	public void testOrInBinding() {
		Regex regex = Regex.str( "a" ).or( Regex.eps() ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) ).build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "a|a" ) );
		assertTrue( moa.check( "|" ) );
		assertFalse( moa.check( "|a" ) );
		assertFalse( moa.check( "a|" ) );
	}

	@Test
	public void plusInBinding() {
		Moa moa = Regex.str( "a" ).plus().bind( "toast" ).and( "b" ).and( Regex.reference( "toast" ) ).build().toMoa();
		assertTrue( moa.check( "aaabaaa" ) );
		assertFalse( moa.check( "aaaba" ) );
		assertFalse( moa.check( "aaab" ) );
	}

	@Test
	public void testSimplePlus() {
		Moa moa = Regex.str( "a" ).plus().build().toMoa();
		assertFalse( moa.check( "" ) );
		assertTrue( moa.check( "a" ) );
		assertTrue( moa.check( "aa" ) );
	}

	@Test
	public void testCool() {
		Moa moa = Regex.reference( "x" )
				.bind( "y" )
				.and( Regex.reference( "y" ).and( "a" ).bind( "x" ) )
				.plus()
				.toMoa();
		assertTrue( moa.check( "aaaa" ) );
		boolean tmp = false;
		for ( int i = 0; i < 100; ++i ) {
			String str = repeat( "a", i );
			boolean res = moa.check( str );
			if ( res ) {
				tmp = true;
				System.out.println( str );
			}
		}
		assertTrue( tmp );
	}

	private static String repeat(String str, int times) {
		String ret = "";
		for ( int i = 0; i < times; ++i ) {
			ret += str;
		}
		return ret;
	}

	@Test
	public void testPlusWithBind() {
		Regex regex = Regex.str( "abc" ).bind( "toast" )
				.and( "|" )
				.and( Regex.reference( "toast" ) ).plus().build();
		Moa moa = regex.toMoa();
		assertTrue( moa.check( "abc|abc" ) );
		assertFalse( moa.check( "abc|abcabc" ) );
		//ensure that the internal regex is reused
		assertTrue( moa.check( "abc|abcabc|abc" ) );
		//ensure that the bind from before is used
		assertFalse( moa.check( "abc|abca|a" ) );
	}

	@Test(expected = IllegalStateException.class)
	public void testDoubleBuild() {
		Regex regex = Regex.str( "str" );
		regex.build( new HashMap<>(), new HashMap<>() );
		regex.build( null, null );
	}

	@Test
	public void testEpsilonMoa() {
		Moa moa = Regex.eps().toMoa();
		assertTrue( moa.check( "" ) );
		assertFalse( moa.check( "t" ) );
	}

}
