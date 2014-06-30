package at.ac.uibk.dps.biohadoop.endpoint;

import at.ac.uibk.dps.biohadoop.queue.Task;

public interface MasterEndpoint {

	public void handleRegistration();

	public void handleWorkInit();

	public void handleWork();

	public Task<?> getCurrentTask();

	public Endpoint getEndpoint();

}
