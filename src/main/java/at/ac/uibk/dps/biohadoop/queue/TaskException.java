package at.ac.uibk.dps.biohadoop.queue;

public class TaskException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public TaskException() {
	}

	public TaskException(String message) {
		super(message);
	}

	public TaskException(Throwable cause) {
		super(cause);
	}

	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}
}
