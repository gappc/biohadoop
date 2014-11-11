package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

public class EndpointException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public EndpointException() {
	}

	public EndpointException(String message) {
		super(message);
	}

	public EndpointException(Throwable cause) {
		super(cause);
	}

	public EndpointException(String message, Throwable cause) {
		super(message, cause);
	}
}
