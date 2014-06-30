package at.ac.uibk.dps.biohadoop.server;

//TODO should be made checked
public class StopServerException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public StopServerException() {
	}

	public StopServerException(String message) {
		super(message);
	}

	public StopServerException(Throwable cause) {
		super(cause);
	}

	public StopServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
