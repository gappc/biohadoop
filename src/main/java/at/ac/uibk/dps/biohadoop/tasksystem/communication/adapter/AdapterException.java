package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

public class AdapterException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public AdapterException() {
	}

	public AdapterException(String message) {
		super(message);
	}

	public AdapterException(Throwable cause) {
		super(cause);
	}

	public AdapterException(String message, Throwable cause) {
		super(message, cause);
	}
}
