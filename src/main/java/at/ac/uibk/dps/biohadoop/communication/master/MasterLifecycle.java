package at.ac.uibk.dps.biohadoop.communication.master;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;

public interface MasterLifecycle {

	public void configure();

	public void start() throws EndpointLaunchException;

	public void stop();
	
}
