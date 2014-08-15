package at.ac.uibk.dps.biohadoop.handler.persistence.file;

public class FileSaveException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public FileSaveException() {
	}

	public FileSaveException(String message) {
		super(message);
	}

	public FileSaveException(Throwable cause) {
		super(cause);
	}

	public FileSaveException(String message, Throwable cause) {
		super(message, cause);
	}
}
