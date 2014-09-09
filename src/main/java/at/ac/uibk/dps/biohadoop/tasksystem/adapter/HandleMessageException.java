package at.ac.uibk.dps.biohadoop.tasksystem.adapter;

public class HandleMessageException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public HandleMessageException() {
	}

	public HandleMessageException(String message) {
		super(message);
	}

	public HandleMessageException(Throwable cause) {
		super(cause);
	}

	public HandleMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
