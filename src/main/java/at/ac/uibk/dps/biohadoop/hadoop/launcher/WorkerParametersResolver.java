package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.NettyServer;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerComm;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;

public class WorkerParametersResolver {

	public static List<String> getWorkerParameters() {
		List<String> workerParameters = new ArrayList<>();
		List<WorkerConfiguration> configurations = Environment
				.getBiohadoopConfiguration().getCommunicationConfiguration()
				.getWorkers();
		
		int count = 0;
		for (WorkerConfiguration configuration : configurations) {
			Class<? extends WorkerComm> worker = configuration.getWorker();
			Integer port = NettyServer.getPort(worker);
			for (int i = 0; i < configuration.getCount(); i++) {
				workerParameters.add(worker.getCanonicalName() + " "
						+ HostInfo.getHostname() + " " + port);
				count++;
			}
		}
		Environment.set(Environment.WORKER_COUNT, Integer.toString(count));
		
		return workerParameters;
	}

}
