package com.github.s4ke.moar.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.s4ke.moar.moa.Moa;
import com.github.s4ke.moar.moa.MoaMatcher;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Martin Braun
 */
public class JavaReplaceBehaviour {

	@Test
	public void testReplaceFirst() {
		//check if the a previous match chanes the outcome
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

}
