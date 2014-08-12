package at.ac.uibk.dps.biohadoop.hadoop.launcher;

//TODO should be made checked
public class WorkerLaunchException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public WorkerLaunchException() {
	}

	public WorkerLaunchException(String message) {
		super(message);
	}

	public WorkerLaunchException(Throwable cause) {
		super(cause);
	}

	public WorkerLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
