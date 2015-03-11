package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.NettyClient;

public abstract class AbstractWorkerComm implements WorkerComm {
	
	protected final NettyClient client = new NettyClient();

}
