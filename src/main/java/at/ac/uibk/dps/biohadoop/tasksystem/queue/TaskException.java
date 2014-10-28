package at.ac.uibk.dps.biohadoop.tasksystem.queue;

/**
 * Thrown to indicate that something went wrong in the Task system of Biohadoop.
 * 
 * @author Christian Gapp
 *
 */
public class TaskException extends RuntimeException {

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
