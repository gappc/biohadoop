package at.ac.uibk.dps.biohadoop.standalone;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);
	
	private Configuration conf = new YarnConfiguration();
	
	@Inject
	private InjectionBean injectedBean;
	
	@Inject
	private JettyServer jettyServer;

	public void run(String[] args) throws Exception {
		logger.info("#################### Injected Bean: " + injectedBean.getRandom());
		jettyServer.startServer();
		Thread.sleep(5000);
		startContainer(args);
		System.out.println("shalla");
		jettyServer.stopServer();
	}
	
	private void startContainer(String[] args) throws Exception {
		final String command = args[0];
		final int n = Integer.valueOf(args[1]);

		// Initialize clients to ResourceManager and NodeManagers
		Configuration conf = new YarnConfiguration();

		AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
		rmClient.init(conf);
		rmClient.start();

		NMClient nmClient = NMClient.createNMClient();
		nmClient.init(conf);
		nmClient.start();

		// Register with ResourceManager
		System.out.println("registerApplicationMaster 0");
		rmClient.registerApplicationMaster("", 0, "");
		System.out.println("registerApplicationMaster 1");

		// Priority for worker containers - priorities are intra-application
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);

		// Resource requirements for worker containers
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);

		// Make container requests to ResourceManager
		for (int i = 0; i < n; ++i) {
			ContainerRequest containerAsk = new ContainerRequest(capability,
					null, null, priority);
			System.out.println("Making res-req " + i);
			rmClient.addContainerRequest(containerAsk);
		}

		// Obtain allocated containers and launch
		int allocatedContainers = 0;
		while (allocatedContainers < n) {
			AllocateResponse response = rmClient.allocate(0);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;

				// Launch container by create ContainerLaunchContext
				ContainerLaunchContext ctx = Records
						.newRecord(ContainerLaunchContext.class);
				ctx.setCommands(Collections.singletonList("$JAVA_HOME/bin/java"
						+ " -Xmx256M"
						+ " at.ac.uibk.dps.biohadoop.SimpleWorker 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
						+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr"));
				
				// Setup jar for ApplicationMaster
				Path jarPath = new Path("hdfs://master:54310/biohadoop/biohadoop-0.0.1-SNAPSHOT.jar");
				logger.info("###### PATH SET TO : " + jarPath);
				LocalResource appMasterJar = Records.newRecord(LocalResource.class);
				setupAppMasterJar(jarPath, appMasterJar);
				ctx.setLocalResources(Collections.singletonMap("biohadoop-container.jar", appMasterJar));
				
				// Setup CLASSPATH for ApplicationMaster
				Map<String, String> appMasterEnv = new HashMap<String, String>();
				setupAppMasterEnv(appMasterEnv);
				ctx.setEnvironment(appMasterEnv);
				
				System.out
						.println("Launching container " + allocatedContainers);
				nmClient.startContainer(container, ctx);
			}
			Thread.sleep(100);
		}

		// Now wait for containers to complete
		int completedContainers = 0;
		while (completedContainers < n) {
			AllocateResponse response = rmClient.allocate(completedContainers
					/ n);
			for (ContainerStatus status : response
					.getCompletedContainersStatuses()) {
				++completedContainers;
				System.out
						.println("Completed container " + completedContainers);
			}
			Thread.sleep(100);
		}

		// Un-register with ResourceManager
		rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED,
				"", "");
	}
	
	private void setupAppMasterJar(Path jarPath, LocalResource appMasterJar)
			throws IOException {
		FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
		appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
		appMasterJar.setSize(jarStat.getLen());
		appMasterJar.setTimestamp(jarStat.getModificationTime());
		appMasterJar.setType(LocalResourceType.FILE);
		appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC);
	}
	
	private void setupAppMasterEnv(Map<String, String> appMasterEnv) {
		for (String c : conf.getStrings(
				YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
					c.trim());
		}
		Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
				Environment.PWD.$() + File.separator + "*");
	}
	
//	private static class EmbeddedServer extends Thread {
//		@Override
//		public void run() {
//			logger.info("############ Starting application master ##########");
//			server = new Server(30000);
//			Context root = new Context(server, "/", Context.SESSIONS);
//			root.addServlet(
//					new ServletHolder(new ServletContainer(
//							new PackagesResourceConfig(
//									"at.ac.uibk.dps.biohadoop.rs"))), "/");
//			try {
//				server.start();
//				server.join();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
}
