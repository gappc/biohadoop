package at.ac.uibk.dps.biohadoop.applicationmanager;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class ApplicationId implements Serializable {

	private static final long serialVersionUID = 6640295799209385434L;
	private final int id;
	
	private ApplicationId() {
		this.id = ThreadLocalRandom.current().nextInt();
	}
	
	private ApplicationId(int id) {
		this.id = id;
	}
	
	public static ApplicationId newInstance() {
		return new ApplicationId();
	}

	public long getId() {
		return id;
	}
	
	public static ApplicationId valueOf(String input) {
		int newId = Integer.valueOf(input);
		return new ApplicationId(newId);
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
	
	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
