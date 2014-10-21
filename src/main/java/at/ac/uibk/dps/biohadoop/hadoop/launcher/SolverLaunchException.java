package at.ac.uibk.dps.biohadoop.hadoop.launcher;

public class SolverLaunchException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public SolverLaunchException() {
	}

	public SolverLaunchException(String message) {
		super(message);
	}

	public SolverLaunchException(Throwable cause) {
		super(cause);
	}

	public SolverLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
