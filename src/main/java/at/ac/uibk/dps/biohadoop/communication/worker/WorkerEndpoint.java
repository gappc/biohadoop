package at.ac.uibk.dps.biohadoop.communication.worker;

import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;


public interface WorkerEndpoint {
	
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration) throws WorkerLaunchException;

	public void configure(String[] args) throws WorkerException;

	public void start() throws WorkerException;
	
}
