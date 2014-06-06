package at.ac.uibk.dps.biohadoop.persistencemanager;

public class PersistenceSaveException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public PersistenceSaveException() {
	}

	public PersistenceSaveException(String message) {
		super(message);
	}

	public PersistenceSaveException(Throwable cause) {
		super(cause);
	}

	public PersistenceSaveException(String message, Throwable cause) {
		super(message, cause);
	}
}
