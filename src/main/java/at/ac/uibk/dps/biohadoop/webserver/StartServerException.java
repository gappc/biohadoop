package at.ac.uibk.dps.biohadoop.webserver;

//TODO should be made checked
public class StartServerException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public StartServerException() {
	}

	public StartServerException(String message) {
		super(message);
	}

	public StartServerException(Throwable cause) {
		super(cause);
	}

	public StartServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
