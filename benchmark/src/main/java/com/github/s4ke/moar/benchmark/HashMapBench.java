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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * @author Martin Braun
 */
@State(Scope.Benchmark)
public class HashMapBench {

	private int[] arr;
	private Map<Integer, Integer> map;
	private static final int ARR_SIZE = 4;

	private static final int GET_COUNT = 10_000;

	@Setup
	public void setup() {
		Random random = new Random(42);
		this.arr = new int[ARR_SIZE];
		this.map = new HashMap<>();
		for(int i = 0; i < ARR_SIZE; ++i) {
			int val = random.nextInt();
			this.arr[i] = val;
			this.map.put(i, val);
		}
	}

	int hack = 0;

	@Benchmark
	public void benchArray() {
		hack = 0;
		Random random = new Random( 43 );
		for(int i = 0; i < GET_COUNT; ++i) {
			hack += this.arr[random.nextInt(ARR_SIZE)];
		}
	}

	@Benchmark
	public void benchMap() {
		hack = 0;
		Random random = new Random( 43 );
		for(int i = 0; i < GET_COUNT; ++i) {
			hack += this.map.get(random.nextInt(ARR_SIZE));
		}
	}

}
