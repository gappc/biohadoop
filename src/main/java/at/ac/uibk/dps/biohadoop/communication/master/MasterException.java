package at.ac.uibk.dps.biohadoop.communication.master;

public class MasterException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public MasterException() {
	}

	public MasterException(String message) {
		super(message);
	}

	public MasterException(Throwable cause) {
		super(cause);
	}

	public MasterException(String message, Throwable cause) {
		super(message, cause);
	}
}
