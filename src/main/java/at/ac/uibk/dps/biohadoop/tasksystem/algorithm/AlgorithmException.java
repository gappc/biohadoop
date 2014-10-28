package at.ac.uibk.dps.biohadoop.tasksystem.algorithm;

/**
 * Thrown to indicate that something went wrong while running an algorithm.
 * 
 * @author Christian Gapp
 *
 */
public class AlgorithmException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;

	public AlgorithmException() {
	}

	public AlgorithmException(String message) {
		super(message);
	}

	public AlgorithmException(Throwable cause) {
		super(cause);
	}

	public AlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}
}
