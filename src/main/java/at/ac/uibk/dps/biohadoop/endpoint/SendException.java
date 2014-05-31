package at.ac.uibk.dps.biohadoop.endpoint;

public class SendException extends RuntimeException {

	private static final long serialVersionUID = 2308781144934182306L;

	public SendException() {
	}

	public SendException(String message) {
		super(message);
	}

	public SendException(Throwable cause) {
		super(cause);
	}

	public SendException(String message, Throwable cause) {
		super(message, cause);
	}
}
