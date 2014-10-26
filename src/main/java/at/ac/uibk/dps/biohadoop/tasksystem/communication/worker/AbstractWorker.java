package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.NettyClient;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;

public abstract class AbstractWorker implements Worker {
	
	protected final NettyClient client = new NettyClient();

}
