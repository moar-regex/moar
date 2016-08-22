package com.github.s4ke.moar;

/**
 * Exception that is thrown if non determinism is found during the compilation or while running the {@link com.github.s4ke.moar.moa.Moa}
 *
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
