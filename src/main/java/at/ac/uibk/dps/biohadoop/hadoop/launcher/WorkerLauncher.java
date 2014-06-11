package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;
import at.ac.uibk.dps.biohadoop.torename.LaunchContainerRunnable;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class WorkerLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerLauncher.class);

	public static void launchWorkers(YarnConfiguration yarnConfiguration,
			BiohadoopConfiguration biohadoopConfig, String configFilename) throws Exception {
		LOG.info("#### startWorker: ");

		// Initialize clients to ResourceManager and NodeManagers
		// Configuration conf = new YarnConfiguration();

		AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
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
		int containerCount = workerList.size();

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

				LOG.info("Launching shell command on a new container."
						+ ", containerId=" + container.getId()
						+ ", containerNode=" + container.getNodeId().getHost()
						+ ":" + container.getNodeId().getPort()
						+ ", containerNodeURI="
						+ container.getNodeHttpAddress()
						+ ", containerResourceMemory"
						+ container.getResource().getMemory()
						+ ", containerResourceVirtualCores"
						+ container.getResource().getVirtualCores());

				// Launch container by create ContainerLaunchContext
				ContainerLaunchContext ctx = Records
						.newRecord(ContainerLaunchContext.class);

				String clientCommand = "$JAVA_HOME/bin/java" + " -Xmx128M"
						+ " " + workerList.get(0) + " "
						+ HostInfo.getHostname() + " " + configFilename + " 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stdout" + " 2>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr";
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

		try {
			LOG.info("Waiting for " + containerCount
					+ " containers to complete");

			ApplicationManager applicationManager = ApplicationManager
					.getInstance();

			int completedContainers = 0;
			while (completedContainers < containerCount) {
				float progress = applicationManager.getOverallProgress();
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

	private static List<String> getWorkerList(BiohadoopConfiguration config) {
		List<String> workerList = new ArrayList<String>();
		for (String key : config.getWorkers().keySet()) {
			int value = config.getWorkers().get(key);
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
}
