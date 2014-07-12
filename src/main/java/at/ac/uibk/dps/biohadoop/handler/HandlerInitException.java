package at.ac.uibk.dps.biohadoop.handler;

public class HandlerInitException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public HandlerInitException() {
	}

	public HandlerInitException(String message) {
		super(message);
	}

	public HandlerInitException(Throwable cause) {
		super(cause);
	}

	public HandlerInitException(String message, Throwable cause) {
		super(message, cause);
	}
}
