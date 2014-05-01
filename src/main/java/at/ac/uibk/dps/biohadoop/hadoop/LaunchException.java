package at.ac.uibk.dps.biohadoop.hadoop;

public class LaunchException extends Exception {

	private static final long serialVersionUID = -101080601229146918L;

	public LaunchException() {
	}

	public LaunchException(String message) {
		super(message);
	}

	public LaunchException(Throwable cause) {
		super(cause);
	}

	public LaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
