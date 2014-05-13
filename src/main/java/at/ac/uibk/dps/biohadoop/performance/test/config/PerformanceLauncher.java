package at.ac.uibk.dps.biohadoop.performance.test.config;

import java.io.File;
import java.io.IOException;
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
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Config;
import at.ac.uibk.dps.biohadoop.hadoop.LaunchException;
import at.ac.uibk.dps.biohadoop.hadoop.Launcher;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LaunchContainerRunnable;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PerformanceLauncher implements Launcher {

	public static int MAX_ITERATIONS = 0;
	private static Logger LOGGER = LoggerFactory.getLogger(PerformanceLauncher.class);
	
	private YarnConfiguration yarnConfiguration = new YarnConfiguration();
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public Config getConfiguration(String configFilename) {
		try {
			return mapper.readValue(HdfsUtil.openFile(yarnConfiguration, configFilename), PerformanceConfig.class);
		} catch (IOException e) {
			LOGGER.error("Could not read configuration {}", configFilename);
			return null;
		}
	}
	
	@Override
	public boolean isConfigurationValid(String configFilename) {
		PerformanceConfig PerformanceConfig = (PerformanceConfig)getConfiguration(configFilename);
		if (PerformanceConfig == null) {
			LOGGER.error("Could not read configuration {}", configFilename);
			return false;
		}
		MAX_ITERATIONS = PerformanceConfig.getAlgorithmConfig().getMaxIterations();
		return true;
	}
	
	@Override
	public void launch(String configFilename) throws LaunchException {
		if (!isConfigurationValid(configFilename)) {
			throw new LaunchException("Configuration " + configFilename + " is not valid");
		}
		
		try {
			PerformanceConfig PerformanceConfig = (PerformanceConfig)getConfiguration(configFilename);
			launchMasterEndpoints(PerformanceConfig);
			
			if (System.getProperty("local") == null) {
				launchWorkers(PerformanceConfig, configFilename);
			}
		} catch (Exception e) {
			throw new LaunchException("Error while launching program", e);
		}
	}

	private void launchMasterEndpoints(PerformanceConfig config) throws Exception {
		for (String masterEndpoint : ((PerformanceConfig)config).getMasterEndpoints()) {
			Class.forName(masterEndpoint).newInstance();
		}
	}

	private void launchWorkers(PerformanceConfig config, String configFilename) throws Exception {
		LOGGER.info("#### startWorker: ");

		// Initialize clients to ResourceManager and NodeManagers
//		Configuration conf = new YarnConfiguration();

		AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
		rmClient.init(yarnConfiguration);
		rmClient.start();

		NMClient nmClient = NMClient.createNMClient();
		nmClient.init(yarnConfiguration);
		nmClient.start();

		// Register with ResourceManager
		LOGGER.info("registerApplicationMaster 0");
		rmClient.registerApplicationMaster("", 0, "");
		LOGGER.info("registerApplicationMaster 1");

		// Priority for worker containers - priorities are intra-application
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);

		// Resource requirements for worker containers
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);

		List<String> workerList = getWorkerList(config);
		int containerCount = workerList.size();
		
		LOGGER.info("Make container requests to ResourceManager");
		for (int i = 0; i < containerCount; ++i) {
			ContainerRequest containerAsk = new ContainerRequest(capability,
					null, null, priority);
			LOGGER.info("Making res-req " + i);
			rmClient.addContainerRequest(containerAsk);
		}

		LOGGER.info("Obtain allocated containers and launch");
		int allocatedContainers = 0;
		while (allocatedContainers < containerCount) {
			AllocateResponse response = rmClient.allocate(0.0f);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;

				LOGGER.info("Launching shell command on a new container."
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
						+ " " + workerList.get(0) + " " + Hostname.getHostname() + " " + configFilename + " 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stdout" + " 2>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr";
				workerList.remove(0);
				LOGGER.info("Client command: " + clientCommand);
				ctx.setCommands(Collections.singletonList(clientCommand));

				String libPath = "hdfs://master:54310/biohadoop/lib/";
				Map<String, LocalResource> jars = LocalResourceBuilder
						.getStandardResources(libPath, yarnConfiguration);
				ctx.setLocalResources(jars);

				// Setup CLASSPATH for ApplicationMaster
				Map<String, String> appMasterEnv = new HashMap<String, String>();
				setupAppMasterEnv(appMasterEnv, yarnConfiguration);
				ctx.setEnvironment(appMasterEnv);

				LOGGER.info("Launching container " + allocatedContainers);

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

		JobManager jobManager = JobManager.getInstance();
		
		try {
			LOGGER.info("Waiting for " + containerCount + " containers to complete");
			int completedContainers = 0;
			while (completedContainers < containerCount) {
				AllocateResponse response = rmClient.allocate(jobManager.getCompleted());
				for (ContainerStatus status : response
						.getCompletedContainersStatuses()) {
					++completedContainers;
					LOGGER.info("Completed container {} with status {}",
							completedContainers, status);
				}
				Thread.sleep(100);
			}
			rmClient.allocate(1.0f);

			LOGGER.info("All containers completed, unregister with ResourceManager");
			rmClient.unregisterApplicationMaster(
					FinalApplicationStatus.SUCCEEDED, "", "");
		} catch (Exception e) {
			LOGGER.error("******** Application error ***********", e);
		}
	}
	
	private List<String> getWorkerList(PerformanceConfig config) {
		List<String> workerList = new ArrayList<String>();
		for (String key :config.getWorkers().keySet()) {
			int value = config.getWorkers().get(key);
			for (int i = 0; i < value; i++) {
				workerList.add(key);
				LOGGER.info("Worker {} added", key);
			}
		}
		return workerList;
	}
	
	private void setupAppMasterEnv(Map<String, String> appMasterEnv,
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