package at.ac.uibk.dps.biohadoop.applicationmanager;

import java.util.Random;

public class ApplicationId {

	private static final Random rand = new Random();
	private final int id;
	
	private ApplicationId() {
		this.id = ApplicationId.rand.nextInt();
	}
	
	public static ApplicationId newInstance() {
		return new ApplicationId();
	}

	public long getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ApplicationId)) {
			return false;
		}
		ApplicationId jobId = (ApplicationId) obj;
		return this.id == jobId.id;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
}
