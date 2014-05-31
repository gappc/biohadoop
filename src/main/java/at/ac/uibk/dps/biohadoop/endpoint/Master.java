package at.ac.uibk.dps.biohadoop.endpoint;

import at.ac.uibk.dps.biohadoop.jobmanager.Task;

public interface Master<T> {

	public void handleRegistration() throws ShutdownException;
	public void handleWorkInit() throws ShutdownException;
	public void handleWork() throws ShutdownException;
	public Task<T> getCurrentTask();
	
}
