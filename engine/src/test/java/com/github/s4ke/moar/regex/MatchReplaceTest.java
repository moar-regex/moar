package com.github.s4ke.moar.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.moa.Moa;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Martin Braun
 */
public class MatchReplaceTest {

	@Test
	public void testReplaceFirst() {
		//check if the a previous match changes the outcome
		//of replaceFirst
		{
			Pattern p = Pattern.compile( "a" );
			Matcher matcher = p.matcher( "aa" );
			matcher.replaceFirst( "b" );
			String res = matcher.replaceFirst( "b" );
			assertEquals( "ba", res );
		}
		//it does not.

		//now check the same for the GenericMoaMatcher
		{
			Regex regex = Regex.str( "a" );
			Moa moa = regex.toMoa();
			MoaMatcher moaMatcher = moa.matcher( "aa" );
			moaMatcher.replaceFirst( "b" );
			String res = moaMatcher.replaceFirst( "b" );
			assertEquals( "ba", res );
		}
	}

	@Test
	public void testReplaceAll() {
		{
			Moa moa = Regex.str( "aa" ).toMoa();
			MoaMatcher matcher = moa.matcher( "aabaabaabaabaa" );
			assertEquals( "bbbb", matcher.replaceAll( "" ) );
			assertEquals( "ccbccbccbccbcc", matcher.replaceAll( "cc" ) );
		}
	}

}
