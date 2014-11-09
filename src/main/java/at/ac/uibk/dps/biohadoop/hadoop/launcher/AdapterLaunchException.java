package at.ac.uibk.dps.biohadoop.hadoop.launcher;

public class AdapterLaunchException extends Exception {

	private static final long serialVersionUID = 2457367887044283294L;

	public AdapterLaunchException() {
	}

	public AdapterLaunchException(String message) {
		super(message);
	}

	public AdapterLaunchException(Throwable cause) {
		super(cause);
	}

	public AdapterLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
}
