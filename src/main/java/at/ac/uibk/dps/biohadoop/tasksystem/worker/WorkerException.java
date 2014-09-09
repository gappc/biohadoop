package at.ac.uibk.dps.biohadoop.tasksystem.worker;

public class WorkerException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public WorkerException() {
	}

	public WorkerException(String message) {
		super(message);
	}

	public WorkerException(Throwable cause) {
		super(cause);
	}

	public WorkerException(String message, Throwable cause) {
		super(message, cause);
	}
}
