package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApplicationMaster {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ApplicationMaster.class);

	public static void main(String[] args) {
		LOGGER.info("############ Starting application master ##########");
		LOGGER.info("############ Starting application master ARGS: " + args);

		if (args.length == 1) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			JsonNode launcherClass = null;
			try {
				JsonNode root = mapper.readTree(new File(args[0]));
				launcherClass = root.findValue("launcherClass");

				Launcher launcher = (Launcher) Class.forName(
						launcherClass.asText()).newInstance();
				launcher.launch(args[0]);
			} catch (Exception e) {
				LOGGER.error("Error while using {} to launch application",
						launcherClass.asText(), e);
			}
		} else {
			LOGGER.error("Wrong number of arguments, got {}, expected 1",
					args.length);
			throw new IllegalArgumentException(
					"Wrong number of arguments, got " + args.length
							+ ", expected 1");
		}

		LOGGER.info("############ Stopping application master ##########");
	}

	// private void startWorker(String configFile, BiohadoopConfig config,
	// BiohadoopLauncher launcher) throws Exception {
	// LOGGER.info("#### startWorker: ");
	//
	// // Initialize clients to ResourceManager and NodeManagers
	// Configuration conf = new YarnConfiguration();
	//
	// AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
	// rmClient.init(conf);
	// rmClient.start();
	//
	// NMClient nmClient = NMClient.createNMClient();
	// nmClient.init(conf);
	// nmClient.start();
	//
	// // Register with ResourceManager
	// LOGGER.info("registerApplicationMaster 0");
	// rmClient.registerApplicationMaster("", 0, "");
	// LOGGER.info("registerApplicationMaster 1");
	//
	// // Priority for worker containers - priorities are intra-application
	// Priority priority = Records.newRecord(Priority.class);
	// priority.setPriority(0);
	//
	// // Resource requirements for worker containers
	// Resource capability = Records.newRecord(Resource.class);
	// capability.setMemory(128);
	// capability.setVirtualCores(1);
	//
	// LOGGER.info("Make container requests to ResourceManager");
	// for (int i = 0; i < n; ++i) {
	// ContainerRequest containerAsk = new ContainerRequest(capability,
	// null, null, priority);
	// LOGGER.info("Making res-req " + i);
	// rmClient.addContainerRequest(containerAsk);
	// }
	//
	// LOGGER.info("Obtain allocated containers and launch");
	// int allocatedContainers = 0;
	// while (allocatedContainers < n) {
	// AllocateResponse response = rmClient.allocate(0.1f);
	// for (Container container : response.getAllocatedContainers()) {
	// ++allocatedContainers;
	//
	// LOGGER.info("Launching shell command on a new container."
	// + ", containerId=" + container.getId()
	// + ", containerNode=" + container.getNodeId().getHost()
	// + ":" + container.getNodeId().getPort()
	// + ", containerNodeURI="
	// + container.getNodeHttpAddress()
	// + ", containerResourceMemory"
	// + container.getResource().getMemory()
	// + ", containerResourceVirtualCores"
	// + container.getResource().getVirtualCores());
	//
	// // Launch container by create ContainerLaunchContext
	// ContainerLaunchContext ctx = Records
	// .newRecord(ContainerLaunchContext.class);
	//
	// String clientCommand = "$JAVA_HOME/bin/java" + " -Xmx128M"
	// + " " + command + " " + Hostname.getHostname() + " 1>"
	// + ApplicationConstants.LOG_DIR_EXPANSION_VAR
	// + "/stdout" + " 2>"
	// + ApplicationConstants.LOG_DIR_EXPANSION_VAR
	// + "/stderr";
	// LOGGER.info("!!!Client command: " + clientCommand);
	// ctx.setCommands(Collections.singletonList(clientCommand));
	//
	// String libPath = "hdfs://master:54310/biohadoop/lib/";
	// Map<String, LocalResource> jars = LocalResourceBuilder
	// .getStandardResources(libPath, conf);
	// ctx.setLocalResources(jars);
	//
	// // Setup CLASSPATH for ApplicationMaster
	// Map<String, String> appMasterEnv = new HashMap<String, String>();
	// setupAppMasterEnv(appMasterEnv, conf);
	// ctx.setEnvironment(appMasterEnv);
	//
	// LOGGER.info("Launching container " + allocatedContainers);
	//
	// // Launch and start the container on a separate thread to keep
	// // the main
	// // thread unblocked as all containers may not be allocated at
	// // one go.
	// LaunchContainerRunnable containerRunnable = new LaunchContainerRunnable(
	// nmClient, container, ctx);
	// Thread launchThread = new Thread(containerRunnable);
	// launchThread.start();
	// }
	// Thread.sleep(100);
	// }
	//
	// try {
	// LOGGER.info("Waiting for " + n + " containers to complete");
	// int completedContainers = 0;
	// while (completedContainers < n) {
	// AllocateResponse response = rmClient.allocate(0.2f);
	// for (ContainerStatus status : response
	// .getCompletedContainersStatuses()) {
	// ++completedContainers;
	// LOGGER.info("Completed container {} with status {}",
	// completedContainers, status);
	// }
	// Thread.sleep(100);
	// }
	//
	// LOGGER.info("All containers completed, unregister with ResourceManager");
	// rmClient.unregisterApplicationMaster(
	// FinalApplicationStatus.SUCCEEDED, "", "");
	// } catch (Exception e) {
	// LOGGER.error("******** Application error ***********", e);
	// }
	// }
	//
	// private void setupAppMasterEnv(Map<String, String> appMasterEnv,
	// Configuration conf) {
	// Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
	// Environment.PWD.$() + File.separator + "*");
	// for (String c : conf.getStrings(
	// YarnConfiguration.YARN_APPLICATION_CLASSPATH,
	// YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
	// Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
	// c.trim());
	// }
	// }

	// private void run(String[] args) {
	// new Thread(new TaskSupervisor(2000),
	// TaskSupervisor.class.getSimpleName()).start();
	//
	// new GaSocketServer();
	// new GaKryoResource();
	// new GaLocalResource();
	// new UndertowServer();
	//
	// if (args.length >= 1 && ("local").equals(args[0])) {
	// try {
	// FileInput fileInput = new FileInput();
	// Tsp tsp = fileInput
	// .readFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
	// DistancesGlobal.setDistances(tsp.getDistances());
	// Ga ga = new Ga();
	// ga.ga(tsp, 10, 10000);
	// } catch (InterruptedException e) {
	// LOGGER.info("Exception while sleep", e);
	// } catch (IOException e) {
	// LOGGER.info("Exception while reading file", e);
	// }
	// } else {
	// Thread algorithmRunner = null;
	// try {
	// FileInput fileInput = new FileInput();
	// final Tsp tsp = fileInput.readFile("att48.tsp");
	// LOGGER.debug("*********** SUCCESSFULLY READ DATA *************");
	// DistancesGlobal.setDistances(tsp.getDistances());
	//
	// algorithmRunner = new Thread(new Runnable() {
	// @Override
	// public void run() {
	// LOGGER.info("Now running GA main");
	// Ga ga = new Ga();
	// try {
	// ga.ga(tsp, 10, 10000);
	// } catch (InterruptedException e) {
	// LOGGER.error("Failure while running GA thread", e);
	// }
	//
	// }
	// });
	// algorithmRunner.setName("AlgorithmRunner");
	// algorithmRunner.start();
	//
	// startWorker(args);
	// } catch (Exception e) {
	// LOGGER.error("Exception while starting worker", e);
	// // TODO handle exception in a better way
	// algorithmRunner.stop();
	// try {
	// JobManager.getInstance().stopAllWorkers();
	// } catch (InterruptedException e1) {
	// LOGGER.error("Error while forced shutdown of all Threads",
	// e1);
	// }
	// }
	// }
	// }
}
