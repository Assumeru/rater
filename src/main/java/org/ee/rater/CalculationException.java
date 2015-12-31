package org.ee.rater;

public class CalculationException extends RuntimeException {
	private static final long serialVersionUID = 7274855801755819961L;

	public CalculationException() {
		super();
	}

	public CalculationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalculationException(String message) {
		super(message);
	}

	public CalculationException(Throwable cause) {
		super(cause);
	}
}
