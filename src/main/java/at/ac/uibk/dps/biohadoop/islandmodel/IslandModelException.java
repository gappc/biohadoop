package at.ac.uibk.dps.biohadoop.islandmodel;

public class IslandModelException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public IslandModelException() {
	}

	public IslandModelException(String message) {
		super(message);
	}

	public IslandModelException(Throwable cause) {
		super(cause);
	}

	public IslandModelException(String message, Throwable cause) {
		super(message, cause);
	}
}
