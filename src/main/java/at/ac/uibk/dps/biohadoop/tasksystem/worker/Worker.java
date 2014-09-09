package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;

public interface Worker {

	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException;

	public void configure(String[] args) throws WorkerException;

	public void start() throws WorkerException, ConnectionRefusedException;

}
