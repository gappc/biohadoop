package at.ac.uibk.dps.biohadoop.tasksystem.adapter;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;

public interface Adapter {

	public void start(String pipelineName) throws AdapterException;

	public void stop() throws AdapterException;

	public Class<? extends Worker> getMatchingWorkerClass();
	
}
