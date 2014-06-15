package at.ac.uibk.dps.biohadoop.deletable;

import java.util.List;

public class AlgorithmCommunication {

	private String identifier;
	private List<ConnectionType> masterConnection;
	private List<WorkerCommunication> workerCommunication;
	private Class<?> masterImplementation;
	private Class<?> workerImplementation;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<ConnectionType> getMasterConnection() {
		return masterConnection;
	}

	public void setMasterCommunication(
			List<ConnectionType> masterCommunication) {
		this.masterConnection = masterCommunication;
	}

	public List<WorkerCommunication> getWorkerCommunication() {
		return workerCommunication;
	}

	public void setWorkerCommunication(
			List<WorkerCommunication> workerCommunication) {
		this.workerCommunication = workerCommunication;
	}

	public Class<?> getMasterImplementation() {
		return masterImplementation;
	}

	public void setMasterImplementation(Class<?> masterImplementation) {
		this.masterImplementation = masterImplementation;
	}

	public Class<?> getWorkerImplementation() {
		return workerImplementation;
	}

	public void setWorkerImplementation(Class<?> workerImplementation) {
		this.workerImplementation = workerImplementation;
	}

}
