package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerComm;

public interface Endpoint {

	public void start() throws EndpointException;

	public void stop() throws EndpointException;

	public Class<? extends WorkerComm> getMatchingWorkerClass();
	
}
