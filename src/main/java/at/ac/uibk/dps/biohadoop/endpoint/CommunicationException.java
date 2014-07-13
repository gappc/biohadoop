package at.ac.uibk.dps.biohadoop.endpoint;

public class CommunicationException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public CommunicationException() {
	}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(Throwable cause) {
		super(cause);
	}

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
}
