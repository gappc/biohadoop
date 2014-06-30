package at.ac.uibk.dps.biohadoop.hadoop.launcher;

//TODO should be made checked
public class EndpointLaunchException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

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
