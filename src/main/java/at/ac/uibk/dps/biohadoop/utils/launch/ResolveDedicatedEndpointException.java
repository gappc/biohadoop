package at.ac.uibk.dps.biohadoop.utils.launch;

public class ResolveDedicatedEndpointException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public ResolveDedicatedEndpointException() {
	}

	public ResolveDedicatedEndpointException(String message) {
		super(message);
	}

	public ResolveDedicatedEndpointException(Throwable cause) {
		super(cause);
	}

	public ResolveDedicatedEndpointException(String message, Throwable cause) {
		super(message, cause);
	}
}
