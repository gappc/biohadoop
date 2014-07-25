package at.ac.uibk.dps.biohadoop.hadoop.launcher;

//TODO should be made checked
public class EndpointException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

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
