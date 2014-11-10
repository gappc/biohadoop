package at.ac.uibk.dps.biohadoop.hadoop.launcher;

public class AlgorithmLaunchException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public AlgorithmLaunchException() {
	}

	public AlgorithmLaunchException(String message) {
		super(message);
	}

	public AlgorithmLaunchException(Throwable cause) {
		super(cause);
	}

	public AlgorithmLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
