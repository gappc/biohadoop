package at.ac.uibk.dps.biohadoop.endpoint;

public class ReceiveException extends Exception {

	private static final long serialVersionUID = -6258224073663825785L;

	public ReceiveException() {
	}

	public ReceiveException(String message) {
		super(message);
	}

	public ReceiveException(Throwable cause) {
		super(cause);
	}

	public ReceiveException(String message, Throwable cause) {
		super(message, cause);
	}
}
