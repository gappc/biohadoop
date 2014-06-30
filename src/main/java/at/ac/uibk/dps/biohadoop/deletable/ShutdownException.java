package at.ac.uibk.dps.biohadoop.deletable;

//TODO should be made checked
public class ShutdownException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public ShutdownException() {
	}

	public ShutdownException(String message) {
		super(message);
	}

	public ShutdownException(Throwable cause) {
		super(cause);
	}

	public ShutdownException(String message, Throwable cause) {
		super(message, cause);
	}
}
