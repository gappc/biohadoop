package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;

public interface MasterEndpoint2 {

	public void handleRegistration();

	public void handleWorkInit();

	public void handleWork();

	public Task<?> getCurrentTask();

	public Endpoint getEndpoint();

}
