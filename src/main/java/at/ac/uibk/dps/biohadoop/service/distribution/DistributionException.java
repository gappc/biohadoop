package at.ac.uibk.dps.biohadoop.service.distribution;

public class DistributionException extends Exception {

	private static final long serialVersionUID = 2984864374422854392L;
	
	public DistributionException() {
	}

	public DistributionException(String message) {
		super(message);
	}

	public DistributionException(Throwable cause) {
		super(cause);
	}

	public DistributionException(String message, Throwable cause) {
		super(message, cause);
	}
}
