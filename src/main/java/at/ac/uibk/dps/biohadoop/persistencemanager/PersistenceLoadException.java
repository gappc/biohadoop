package at.ac.uibk.dps.biohadoop.persistencemanager;

public class PersistenceLoadException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public PersistenceLoadException() {
	}

	public PersistenceLoadException(String message) {
		super(message);
	}

	public PersistenceLoadException(Throwable cause) {
		super(cause);
	}

	public PersistenceLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
