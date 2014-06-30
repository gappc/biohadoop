package at.ac.uibk.dps.biohadoop.connection;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;

public interface MasterConnection {

	public void configure();

	public void start() throws EndpointLaunchException;

	public void stop();
	
}
