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
package com.github.s4ke.moar.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.util.CharSeq;
import com.github.s4ke.moar.util.IntCharSeq;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 * @author Martin Braun
 */
@State(Scope.Benchmark)
public class MoaPatternBench {

	private List<MoaPattern> patterns = new ArrayList<>();
	private CharSeq sonnets;
	private CharSeq[] easyMatches;
	private final MoaPattern twoPowerOfN = MoaPattern.compile( Regex.TWO_TO_POWER_OF_N_MOA );

	@Setup
	public void setup() {
		for ( String str : Regex.REGEX_TO_BENCH ) {
			patterns.add( MoaPattern.compile( str ) );
		}
		try (BufferedReader reader = new BufferedReader( new InputStreamReader( Main.class.getResourceAsStream(
				"/128sonnets.txt" ) ) )) {
			StringBuilder builder = new StringBuilder();
			String str;
			while ( (str = reader.readLine()) != null ) {
				builder.append( str ).append( "\n" );
			}
			this.sonnets = new IntCharSeq( builder.toString() );
		}
		catch (IOException e) {
			throw new AssertionError( e );
		}

		this.easyMatches = new CharSeq[Regex.EASY_MATCHES.length];
		for(int i = 0; i < Regex.EASY_MATCHES.length; ++i) {
			this.easyMatches[i] = new IntCharSeq( Regex.EASY_MATCHES[i] );
		}

	}

	int matches = 0;

	@TearDown
	public void tearDown() {
		System.out.println(this.matches);
	}

	@Benchmark
	public void benchMoaPattern() {
		this.matches = 0;
		for ( MoaPattern pattern : this.patterns ) {
			MoaMatcher matcher = pattern.matcher( this.sonnets );
			while ( matcher.nextMatch() ) {
				++matches;
			}
		}
	}

	@Benchmark
	public void benchMoaPatternEasy() {
		for ( MoaPattern pattern : this.patterns ) {
			for ( CharSeq easy : this.easyMatches ) {
				if ( !pattern.matcher( easy ).matches() ) {
					//throw new AssertionError( pattern + " did not match " + easy );
				}
			}
		}
	}

	@Benchmark
	public void benchMoaTwoPowerN() {
		this.matches = 0;
		for(int i = 0; i < Regex.TWO_TO_POWER_OF_N_AND_OTHERS_CHARSEQ.length; ++i) {
			if(this.twoPowerOfN.matcher( Regex.TWO_TO_POWER_OF_N_AND_OTHERS_CHARSEQ[i] ).matches()) {
				++this.matches;
			}
		}
		if(this.matches == 0) {
			throw new AssertionError();
		}
	}

}
