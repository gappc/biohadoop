package at.ac.uibk.dps.biohadoop.algorithm;

import java.io.Serializable;
import java.util.UUID;

public class AlgorithmId implements Serializable {

	private static final long serialVersionUID = 8506626310953326430L;
	
	private final UUID id;
	
	private AlgorithmId() {
		this.id = UUID.randomUUID();
	}
	
	private AlgorithmId(UUID id) {
		this.id = id;
	}
	
	public static AlgorithmId newInstance() {
		return new AlgorithmId();
	}

	public static AlgorithmId valueOf(String input) {
		UUID id = UUID.fromString(input);
		return new AlgorithmId(id);
	}
	
	public UUID getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AlgorithmId)) {
			return false;
		}
		AlgorithmId algorithmId = (AlgorithmId) obj;
		return id.equals(algorithmId.id);
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
