package at.ac.uibk.dps.biohadoop.tasksystem;

public class ComputeException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public ComputeException() {
	}

	public ComputeException(String message) {
		super(message);
	}

	public ComputeException(Throwable cause) {
		super(cause);
	}

	public ComputeException(String message, Throwable cause) {
		super(message, cause);
	}
}
