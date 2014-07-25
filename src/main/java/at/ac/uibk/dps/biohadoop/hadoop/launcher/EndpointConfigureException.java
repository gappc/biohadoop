package at.ac.uibk.dps.biohadoop.hadoop.launcher;

//TODO should be made checked
public class EndpointConfigureException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public EndpointConfigureException() {
	}

	public EndpointConfigureException(String message) {
		super(message);
	}

	public EndpointConfigureException(Throwable cause) {
		super(cause);
	}

	public EndpointConfigureException(String message, Throwable cause) {
		super(message, cause);
	}
}
