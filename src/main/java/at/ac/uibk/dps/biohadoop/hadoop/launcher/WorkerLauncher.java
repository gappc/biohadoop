package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerParameter;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerStarter;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.LaunchContainerRunnable;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

//TODO make more parallel and use Callable instead of Thread
public class WorkerLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerLauncher.class);

	private WorkerLauncher() {
	}

	public static void launchWorkers(YarnConfiguration yarnConfiguration,
			BiohadoopConfiguration biohadoopConfig, String configFilename)
			throws Exception {
		LOG.info("#### startWorker: ");

		// Initialize clients to ResourceManager and NodeManagers
		final AMRMClient<ContainerRequest> rmClient = AMRMClient
				.createAMRMClient();
		rmClient.init(yarnConfiguration);
		rmClient.start();

		NMClient nmClient = NMClient.createNMClient();
		nmClient.init(yarnConfiguration);
		nmClient.start();

		// Register with ResourceManager
		LOG.info("registerApplicationMaster 0");
		rmClient.registerApplicationMaster("", 0, "");
		LOG.info("registerApplicationMaster 1");

		// Priority for worker containers - priorities are intra-application
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);

		// Resource requirements for worker containers
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);

		List<String> workerList = getWorkerList(biohadoopConfig);
		List<WorkerParameter> workerParameters = new ArrayList<>();
		for (Iterator<String> it = workerList.iterator(); it.hasNext();) {
			String workerName = it.next();
			WorkerParameter workerParameter = (WorkerParameter) Class.forName(workerName).newInstance();
			if (workerParameter.getWorkerParameters() == null) {
				it.remove();
			}
			else {
				workerParameters.add(workerParameter);
			}
		}

		final int containerCount = workerList.size();

		LOG.info("Make container requests to ResourceManager");
		for (int i = 0; i < containerCount; ++i) {
			ContainerRequest containerAsk = new ContainerRequest(capability,
					null, null, priority);
			LOG.info("Making res-req " + i);
			rmClient.addContainerRequest(containerAsk);
		}

		LOG.info("Obtain allocated containers and launch");
		int allocatedContainers = 0;
		while (allocatedContainers < containerCount) {
			AllocateResponse response = rmClient.allocate(0.0f);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;

				LOG.info(
						"Launching shell command on a new container, containerId={}, containerNode={}:{}, containerNodeURI={}, containerResourceMemory={}, containerResourceVirtualCores={}",
						container.getId(), container.getNodeId().getHost(),
						container.getNodeId().getPort(), container
								.getNodeHttpAddress(), container.getResource()
								.getMemory(), container.getResource()
								.getVirtualCores());

				// Launch container by create ContainerLaunchContext
				ContainerLaunchContext ctx = Records
						.newRecord(ContainerLaunchContext.class);

//				WorkerParameter worker = (WorkerParameter) Class.forName(
//						workerList.get(0)).newInstance();
//				String parameters = worker.getWorkerParameters();
				String parameters = workerParameters.get(0).getWorkerParameters();

				String clientCommand = String
						.format("$JAVA_HOME/bin/java -Xmx128M %s %s %s configFilename 1>%s/stdout 2>%s/stderr",
								WorkerStarter.class.getCanonicalName(),
								workerList.get(0), parameters,
								ApplicationConstants.LOG_DIR_EXPANSION_VAR,
								ApplicationConstants.LOG_DIR_EXPANSION_VAR);

				workerList.remove(0);
				LOG.info("Client command: " + clientCommand);
				ctx.setCommands(Collections.singletonList(clientCommand));

				String libPath = "hdfs://master:54310/biohadoop/lib/";
				Map<String, LocalResource> jars = LocalResourceBuilder
						.getStandardResources(libPath, yarnConfiguration);
				ctx.setLocalResources(jars);

				// Setup CLASSPATH for ApplicationMaster
				Map<String, String> appMasterEnv = new HashMap<String, String>();
				setupAppMasterEnv(appMasterEnv, yarnConfiguration);
				ctx.setEnvironment(appMasterEnv);

				LOG.info("Launching container " + allocatedContainers);

				// Launch and start the container on a separate thread to keep
				// the main
				// thread unblocked as all containers may not be allocated at
				// one go.
				LaunchContainerRunnable containerRunnable = new LaunchContainerRunnable(
						nmClient, container, ctx);
				Thread launchThread = new Thread(containerRunnable);
				launchThread.start();
			}
			Thread.sleep(100);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					LOG.info("Waiting for " + containerCount
							+ " containers to complete");

					SolverService solverService = SolverService.getInstance();

					int completedContainers = 0;
					while (completedContainers < containerCount) {
						float progress = solverService.getOverallProgress();
						AllocateResponse response = rmClient.allocate(progress);
						for (ContainerStatus status : response
								.getCompletedContainersStatuses()) {
							++completedContainers;
							LOG.info("Completed container {} with status {}",
									completedContainers, status);
						}
						Thread.sleep(100);
					}
					rmClient.allocate(1.0f);

					LOG.info("All containers completed, unregister with ResourceManager");
					rmClient.unregisterApplicationMaster(
							FinalApplicationStatus.SUCCEEDED, "", "");
				} catch (Exception e) {
					LOG.error("******** Application error ***********", e);
				}
			}
		}).start();
	}

	private static List<String> getWorkerList(BiohadoopConfiguration config) {
		List<String> workerList = new ArrayList<>();
		CommunicationConfiguration communicationConfiguration = config
				.getCommunicationConfiguration();
		for (String key : communicationConfiguration.getWorkerEndpoints()
				.keySet()) {
			int value = communicationConfiguration.getWorkerEndpoints()
					.get(key);
			for (int i = 0; i < value; i++) {
				workerList.add(key);
				LOG.info("Worker {} added", key);
			}
		}
		return workerList;
	}

	private static void setupAppMasterEnv(Map<String, String> appMasterEnv,
			Configuration conf) {
		Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
				Environment.PWD.$() + File.separator + "*");
		for (String c : conf.getStrings(
				YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
					c.trim());
		}
	}

	public static void pretendToLaunchWorkers(
			BiohadoopConfiguration biohadoopConfiguration) {
		List<String> workerList = getWorkerList(biohadoopConfiguration);
		for (String workerClass : workerList) {
			try {
				WorkerParameter worker = (WorkerParameter) Class.forName(
						workerClass).newInstance();
				String parameters = worker.getWorkerParameters();

				String clientCommand = String
						.format("$JAVA_HOME/bin/java -Xmx128M %s configFilename 1>%s/stdout 2>%s/stderr",
								parameters,
								ApplicationConstants.LOG_DIR_EXPANSION_VAR,
								ApplicationConstants.LOG_DIR_EXPANSION_VAR);

				LOG.info("Launching worker {} with command: {}", workerClass,
						clientCommand);
			} catch (Exception e) {
				LOG.error("Error while pretending to run workers", e);
			}

		}

	}
}
