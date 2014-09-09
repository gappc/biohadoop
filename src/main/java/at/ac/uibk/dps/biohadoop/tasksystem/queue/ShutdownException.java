package at.ac.uibk.dps.biohadoop.tasksystem.queue;

public class ShutdownException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public ShutdownException() {
	}

	public ShutdownException(String message) {
		super(message);
	}

	public ShutdownException(Throwable cause) {
		super(cause);
	}

	public ShutdownException(String message, Throwable cause) {
		super(message, cause);
	}
}
