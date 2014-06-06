package at.ac.uibk.dps.biohadoop.config;

public class BuildParameterException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public BuildParameterException() {
	}

	public BuildParameterException(String message) {
		super(message);
	}

	public BuildParameterException(Throwable cause) {
		super(cause);
	}

	public BuildParameterException(String message, Throwable cause) {
		super(message, cause);
	}
}
