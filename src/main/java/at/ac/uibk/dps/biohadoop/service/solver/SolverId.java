package at.ac.uibk.dps.biohadoop.service.solver;

import java.io.Serializable;
import java.util.UUID;

public class SolverId implements Serializable {

	private static final long serialVersionUID = 8506626310953326430L;
	
	private final UUID id;
	
	private SolverId() {
		this.id = UUID.randomUUID();
	}
	
	private SolverId(UUID id) {
		this.id = id;
	}
	
	public static SolverId newInstance() {
		return new SolverId();
	}

	public static SolverId valueOf(String input) {
		UUID id = UUID.fromString(input);
		return new SolverId(id);
	}
	
	public UUID getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SolverId)) {
			return false;
		}
		SolverId solverId = (SolverId) obj;
		return id.equals(solverId.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return id.toString();
	}
}
