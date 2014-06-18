package at.ac.uibk.dps.biohadoop.service.job;

import java.util.concurrent.ThreadLocalRandom;

public class JobId {
	private final int id;

	private JobId() {
		this.id = ThreadLocalRandom.current().nextInt();
	}

	public static JobId newInstance() {
		return new JobId();
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return new Long(id).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobId)) {
			return false;
		}
		JobId jobId = (JobId) obj;
		return this.id == jobId.id;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
}
