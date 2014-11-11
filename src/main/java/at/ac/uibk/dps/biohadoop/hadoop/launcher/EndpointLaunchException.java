package at.ac.uibk.dps.biohadoop.hadoop.launcher;

public class EndpointLaunchException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public EndpointLaunchException() {
	}

	public EndpointLaunchException(String message) {
		super(message);
	}

	public EndpointLaunchException(Throwable cause) {
		super(cause);
	}

	public EndpointLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
