package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;

public class WorkerParametersResolver {

	private static Logger LOG = LoggerFactory
			.getLogger(WorkerParametersResolver.class);

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<String> getAllWorkerParameters(
			CommunicationConfiguration communicationConfiguration)
			throws WorkerLaunchException {
		List<String> workerParameters = new ArrayList<>();
		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			for (int i = 0; i < workerConfiguration.getCount(); i++) {
				String workerParameter = getWorkerParameters(workerConfiguration);
				if (workerParameter != null) {
					LOG.debug("Adding worker parameters {}", workerParameter);
					workerParameters.add(workerParameter);
				}
				else {
					LOG.warn("Ignoring empty worker parameters for configuration {}", workerConfiguration);
				}
			}
		}
		return workerParameters;
	}

	private static String getWorkerParameters(
			WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		try {
			WorkerEndpoint workerEndpoint = workerConfiguration.getWorker()
					.newInstance();
			return workerEndpoint.buildLaunchArguments(workerConfiguration);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new WorkerLaunchException(
					"Could net get worker parameters for WorkerConfiguration: "
							+ workerConfiguration, e);
		}
	}
	
}
