package at.ac.uibk.dps.biohadoop.distributionmanager;

public class DataMergeException extends Exception {

	private static final long serialVersionUID = -1545479127527654100L;

	public DataMergeException() {
	}

	public DataMergeException(String message) {
		super(message);
	}

	public DataMergeException(Throwable cause) {
		super(cause);
	}

	public DataMergeException(String message, Throwable cause) {
		super(message, cause);
	}
}
