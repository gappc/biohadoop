package at.ac.uibk.dps.biohadoop.service.job.queue;

public class QueueException extends RuntimeException {

	private static final long serialVersionUID = -8494265330034875656L;

	public QueueException() {
	}

	public QueueException(String message) {
		super(message);
	}

	public QueueException(Throwable cause) {
		super(cause);
	}

	public QueueException(String message, Throwable cause) {
		super(message, cause);
	}
}
