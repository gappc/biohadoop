package at.ac.uibk.dps.biohadoop.communication.master;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointConfigureException;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public interface MasterLifecycle {

	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> master) throws EndpointConfigureException;

	public void start() throws EndpointLaunchException;

	public void stop();
	
}
