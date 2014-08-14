package at.ac.uibk.dps.biohadoop.communication.worker;

public class ConnectionRefusedException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public ConnectionRefusedException() {
	}

	public ConnectionRefusedException(String message) {
		super(message);
	}

	public ConnectionRefusedException(Throwable cause) {
		super(cause);
	}

	public ConnectionRefusedException(String message, Throwable cause) {
		super(message, cause);
	}
}
