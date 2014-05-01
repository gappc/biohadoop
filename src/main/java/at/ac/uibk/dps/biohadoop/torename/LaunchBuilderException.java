package at.ac.uibk.dps.biohadoop.torename;

public class LaunchBuilderException extends Exception {

	private static final long serialVersionUID = -101080601229146918L;

	public LaunchBuilderException() {
	}

	public LaunchBuilderException(String message) {
		super(message);
	}

	public LaunchBuilderException(Throwable cause) {
		super(cause);
	}

	public LaunchBuilderException(String message, Throwable cause) {
		super(message, cause);
	}
}
