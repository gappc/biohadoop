package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.NettyServer;

public abstract class AbstractEndpoint implements Endpoint {
	
	protected final NettyServer server = new NettyServer();

	@Override
	public void stop() throws EndpointException {
		server.stopServer();
	}
}
