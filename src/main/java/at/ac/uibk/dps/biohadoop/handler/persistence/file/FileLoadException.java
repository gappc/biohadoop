package at.ac.uibk.dps.biohadoop.handler.persistence.file;

public class FileLoadException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public FileLoadException() {
	}

	public FileLoadException(String message) {
		super(message);
	}

	public FileLoadException(Throwable cause) {
		super(cause);
	}

	public FileLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
