package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.NettyServer;

public abstract class AbstractAdapter implements Adapter {
	
	protected final NettyServer server = new NettyServer();

	@Override
	public void stop() throws AdapterException {
		server.stopServer();
	}
}
