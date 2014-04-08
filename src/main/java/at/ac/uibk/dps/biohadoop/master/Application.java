package at.ac.uibk.dps.biohadoop.master;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;

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

import at.ac.uibk.dps.biohadoop.ga.FileInput;
import at.ac.uibk.dps.biohadoop.ga.Ga;
import at.ac.uibk.dps.biohadoop.ga.Tsp;
import at.ac.uibk.dps.biohadoop.server.UndertowServer;
import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class Application {

	@Inject
	Ga ga;

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public void run(String[] args) {
		UndertowServer loader = new UndertowServer();
		try {
			loader.startServer();
		} catch (IllegalArgumentException | IOException | ServletException e) {
			logger.error("Could not start server", e);
		}

		if (args.length >= 1 && ("local").equals(args[0])) {
			try {
				FileInput fileInput = new FileInput();
				Tsp tsp = fileInput
						.readFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
				DistancesGlobal.setDistances(tsp.getDistances());
				ga.ga(tsp, 10, 100000);
				Thread.sleep(3000000);
			} catch (InterruptedException e) {
				logger.info("Exception while sleep", e);
			} catch (IOException e) {
				logger.info("Exception while reading file", e);
			}
		} else {
			try {
				startWorker(args);
				loader.stopServer();
			} catch (Exception e) {
				logger.info("Exception while starting worker", e);
			}
		}
	}

	private void startWorker(String[] args) throws Exception {
		final String command = args[0];
		final int n = Integer.valueOf(args[1]);

		logger.info("#### COMMAND: " + command);
		logger.info("#### NUMBER OF HOSTS: " + n);

		// Initialize clients to ResourceManager and NodeManagers
		Configuration conf = new YarnConfiguration();

		AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
		rmClient.init(conf);
		rmClient.start();

		NMClient nmClient = NMClient.createNMClient();
		nmClient.init(conf);
		nmClient.start();

		// Register with ResourceManager
		logger.info("registerApplicationMaster 0");
		rmClient.registerApplicationMaster("", 0, "");
		logger.info("registerApplicationMaster 1");

		// Priority for worker containers - priorities are intra-application
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);

		// Resource requirements for worker containers
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);

		logger.info("Make container requests to ResourceManager");
		for (int i = 0; i < n; ++i) {
			ContainerRequest containerAsk = new ContainerRequest(capability,
					null, null, priority);
			logger.info("Making res-req " + i);
			rmClient.addContainerRequest(containerAsk);
		}

		logger.info("Obtain allocated containers and launch");
		int allocatedContainers = 0;
		while (allocatedContainers < n) {
			AllocateResponse response = rmClient.allocate(0.1f);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;

				logger.info("Launching shell command on a new container."
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
						+ " at.ac.uibk.dps.biohadoop.worker.SimpleWorker "
						+ Hostname.getHostname() + " 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stdout" + " 2>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr";
				logger.info("!!!Client command: " + clientCommand);
				ctx.setCommands(Collections.singletonList(clientCommand));

				String libPath = "hdfs://master:54310/biohadoop/lib/";
				Map<String, LocalResource> jars = LocalResourceBuilder
						.getStandardResources(libPath, conf);
				ctx.setLocalResources(jars);

				// Setup CLASSPATH for ApplicationMaster
				Map<String, String> appMasterEnv = new HashMap<String, String>();
				setupAppMasterEnv(appMasterEnv, conf);
				ctx.setEnvironment(appMasterEnv);

				logger.info("Launching container " + allocatedContainers);

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
			logger.info("Waiting for " + n + " containers to complete");
			int completedContainers = 0;
			while (completedContainers < n) {
				// AllocateResponse response =
				// rmClient.allocate(completedContainers
				// / n);
				AllocateResponse response = rmClient.allocate(0.2f);
				for (ContainerStatus status : response
						.getCompletedContainersStatuses()) {
					++completedContainers;
					logger.info("Completed container " + completedContainers);
				}
				Thread.sleep(100);
			}

			logger.info("All containers completed, unregister with ResourceManager");
			rmClient.unregisterApplicationMaster(
					FinalApplicationStatus.SUCCEEDED, "", "");
		} catch (Exception e) {
			logger.error("******** Application error ***********", e);
		}
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
