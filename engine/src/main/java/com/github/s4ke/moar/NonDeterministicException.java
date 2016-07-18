package com.github.s4ke.moar;

/**
 * @author Martin Braun
 */
public class NonDeterministicException extends RuntimeException {

	public NonDeterministicException(String message) {
		super( message );
	}

	public NonDeterministicException(String message, Throwable cause) {
		super( message, cause );
	}
}
