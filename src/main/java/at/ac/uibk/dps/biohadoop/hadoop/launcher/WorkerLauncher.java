package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmService;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.LaunchContainerRunnable;
import at.ac.uibk.dps.biohadoop.hadoop.LocalResourceBuilder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerStarter;

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

		List<String> workerParameters = WorkerParametersResolver.getWorkerParameters();

		final int containerCount = workerParameters.size();

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

				String clientCommand = String
						.format("$JAVA_HOME/bin/java -Xmx128M %s %s 1>%s/stdout 2>%s/stderr",
								WorkerStarter.class.getCanonicalName(),
								workerParameters.get(0),
								ApplicationConstants.LOG_DIR_EXPANSION_VAR,
								ApplicationConstants.LOG_DIR_EXPANSION_VAR);

				workerParameters.remove(0);
				LOG.info("Client command for container {}: {}",
						container.getId(), clientCommand);
				ctx.setCommands(Collections.singletonList(clientCommand));

				Map<String, LocalResource> resources = buildResourcePaths(
						yarnConfiguration, biohadoopConfig.getIncludePaths());
				ctx.setLocalResources(resources);

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

					boolean hasErrors = false;
					int completedContainers = 0;
					while (completedContainers < containerCount) {
						float progress = AlgorithmService.getOverallProgress();
						AllocateResponse response = rmClient.allocate(progress);
						LOG.debug("Waiting for {} containers", containerCount
								- completedContainers);
						for (ContainerStatus status : response
								.getCompletedContainersStatuses()) {
							++completedContainers;
							LOG.info("Completed container {} with status {}",
									status.getContainerId(), status);
							if (status.getExitStatus() == 1) {
								hasErrors = true;
								LOG.error(
										"Container {} exited with an exit code of {} ",
										status.getContainerId(),
										status.getExitStatus());
							}
						}
						Thread.sleep(100);
					}
					rmClient.allocate(1.0f);

					if (!hasErrors) {
						LOG.info("All containers completed, unregister with ResourceManager");
						rmClient.unregisterApplicationMaster(
								FinalApplicationStatus.SUCCEEDED, "", "");
					} else {
						LOG.info("All containers completed but some had non-zero exit code, unregister with ResourceManager");
						rmClient.unregisterApplicationMaster(
								FinalApplicationStatus.FAILED, "", "");
					}
				} catch (Exception e) {
					LOG.error("******** Application error ***********", e);
				}
			}
		}).start();
	}

	private static void setupAppMasterEnv(Map<String, String> appMasterEnv,
			Configuration conf) {
		Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
				Environment.PWD.$() + File.separator + "*", File.pathSeparator);
		for (String c : conf.getStrings(
				YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
					c.trim(), File.pathSeparator);
		}
	}

	private static Map<String, LocalResource> buildResourcePaths(
			YarnConfiguration yarnConfiguration, List<String> includePaths)
			throws WorkerLaunchException, IOException {
		String fsName = null;
		fsName = yarnConfiguration.get("fs.defaultFS");
		if (fsName == null) {
			LOG.debug("fs.defaultFS not set, trying fs.default.name");
			fsName = yarnConfiguration.get("fs.default.name");
			if (fsName != null) {
				LOG.warn("Found fs.default.name which is deprecated, consider replacing it with fs.defaultFS");
			} else {
				throw new WorkerLaunchException(
						"Neither fs.defaultFS nor fs.default.name found in YarnConfiguration");
			}
		}
		if (!fsName.endsWith("/")) {
			fsName += "/";
		}
		
		LOG.debug("Root of libpath is {}", fsName);

		Map<String, LocalResource> resources = new HashMap<>();

		for (String includePath : includePaths) {
			String libPath = fsName;
			if (includePath.startsWith("/")) {
				libPath += includePath.substring(1);
			} else {
				libPath += includePath;
			}
			LOG.debug("Including libPath: {}", libPath);
			Map<String, LocalResource> includePathResources = LocalResourceBuilder
					.getStandardResources(libPath, yarnConfiguration);
			resources.putAll(includePathResources);
		}

		return resources;
	}

	public static void pretendToLaunchWorkers(
			BiohadoopConfiguration biohadoopConfiguration)
			throws WorkerLaunchException {
		List<String> workerParameters = WorkerParametersResolver.getWorkerParameters();
				
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (final String workerParameter : workerParameters) {
			String clientCommand = String
					.format("$JAVA_HOME/bin/java -Xmx128M %s %s 1>%s/stdout 2>%s/stderr",
							WorkerStarter.class.getCanonicalName(),
							workerParameter,
							ApplicationConstants.LOG_DIR_EXPANSION_VAR,
							ApplicationConstants.LOG_DIR_EXPANSION_VAR);

			LOG.info("Launching worker {} with parameters: {}",
					workerParameter, clientCommand);

			executorService.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					WorkerStarter.main(workerParameter.split(" "));
					return null;
				}
			});
		}

		executorService.shutdown();
	}

}
