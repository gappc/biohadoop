package at.ac.uibk.dps.biohadoop.communication.master;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;

public interface MasterEndpoint {

	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> master) throws MasterException;

	public void start() throws MasterException;

	public void stop();
	
}
