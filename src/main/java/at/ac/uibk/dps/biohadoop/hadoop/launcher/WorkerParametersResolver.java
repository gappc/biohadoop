package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;

public class WorkerParametersResolver {

	private static Logger LOG = LoggerFactory
			.getLogger(WorkerParametersResolver.class);

	public static List<String> getWorkerParameters() {
		List<String> workerParameters = new ArrayList<>();
		List<WorkerConfiguration> configurations = Environment
				.getBiohadoopConfiguration().getCommunicationConfiguration()
				.getWorkerConfigurations();
		for (WorkerConfiguration configuration : configurations) {
			Class<? extends Worker> worker = configuration.getWorker();
			Integer port = AbstractAdapter.getPort(worker,
					configuration.getPipelineName());
			for (int i = 0; i < configuration.getCount(); i++) {
				workerParameters.add(worker.getCanonicalName() + " "
						+ HostInfo.getHostname() + " " + port);
			}
		}
		return workerParameters;
	}

}
