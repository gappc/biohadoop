package at.ac.uibk.dps.biohadoop.service.solver;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class SolverId implements Serializable {

	private static final long serialVersionUID = 8506626310953326430L;
	
	private final int id;
	
	private SolverId() {
		this.id = ThreadLocalRandom.current().nextInt();
	}
	
	private SolverId(int id) {
		this.id = id;
	}
	
	public static SolverId newInstance() {
		return new SolverId();
	}

	public long getId() {
		return id;
	}
	
	public static SolverId valueOf(String input) {
		int newId = Integer.valueOf(input);
		return new SolverId(newId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SolverId)) {
			return false;
		}
		SolverId jobId = (SolverId) obj;
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
