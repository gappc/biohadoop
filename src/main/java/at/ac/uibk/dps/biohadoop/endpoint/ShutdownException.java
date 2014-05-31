package at.ac.uibk.dps.biohadoop.endpoint;

public class ShutdownException extends RuntimeException {

	private static final long serialVersionUID = 2308781144934182306L;

	public ShutdownException() {
	}

	public ShutdownException(String message) {
		super(message);
	}

	public ShutdownException(Throwable cause) {
		super(cause);
	}

	public ShutdownException(String message, Throwable cause) {
		super(message, cause);
	}
}
