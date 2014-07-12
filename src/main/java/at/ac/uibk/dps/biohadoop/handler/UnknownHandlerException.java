package at.ac.uibk.dps.biohadoop.handler;

public class UnknownHandlerException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public UnknownHandlerException() {
	}

	public UnknownHandlerException(String message) {
		super(message);
	}

	public UnknownHandlerException(Throwable cause) {
		super(cause);
	}

	public UnknownHandlerException(String message, Throwable cause) {
		super(message, cause);
	}
}
