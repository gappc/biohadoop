package at.ac.uibk.dps.biohadoop.master;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import at.ac.uibk.dps.biohadoop.server.ServerLoader;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);
	
	private Configuration conf = new YarnConfiguration();
	
	private String libPath = "hdfs://master:54310/biohadoop/lib/";
	
	public void run(String[] args) {
		ServerLoader loader = new ServerLoader();
		loader.startServer();
		
		conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
		conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
		conf.set("fs.defaultFS", "hdfs://master:54310");
		
		if (args.length >= 1 && ("local").equals(args[0])) {
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				logger.info("Exception while sleep", e);
			}
		}
		else {
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

		conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
		conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
		conf.set("fs.defaultFS", "hdfs://master:54310");
		conf.set("yarn.resourcemanager.scheduler.address", "master:8030");

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
			AllocateResponse response = rmClient.allocate(0);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;

				// Launch container by create ContainerLaunchContext
				ContainerLaunchContext ctx = Records
						.newRecord(ContainerLaunchContext.class);
				
				String clientCommand = "$JAVA_HOME/bin/java"
						+ " -Xmx256M"
						+ " at.ac.uibk.dps.biohadoop.worker.SimpleWorker "
						+ Hostname.getHostname()
						+ " 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
						+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr";
				logger.info("!!!Client command: " + clientCommand);
				ctx.setCommands(Collections.singletonList(clientCommand));
				
				Map<String, LocalResource> jars = LocalResourceBuilder.builder()
						.setPath(libPath).setConfiguration(conf)
						.addFile("biohadoop-0.0.1-SNAPSHOT.jar")
						.addFile("activation-1.1.jar")
						.addFile("async-http-servlet-3.0-3.0.6.Final.jar")
						.addFile("cdi-api-1.1.jar")
						.addFile("commons-cli-1.2.jar")
						.addFile("commons-codec-1.4.jar")
						.addFile("commons-collections-3.2.1.jar")
						.addFile("commons-configuration-1.6.jar")
						.addFile("commons-io-2.4.jar")
						.addFile("commons-lang-2.6.jar")
						.addFile("commons-logging-1.1.3.jar")
						.addFile("guava-11.0.2.jar")
						.addFile("hadoop-auth-2.3.0.jar")
						.addFile("hadoop-common-2.3.0.jar")
						.addFile("hadoop-hdfs-2.3.0.jar")
						.addFile("hadoop-yarn-api-2.3.0.jar")
						.addFile("hadoop-yarn-client-2.3.0.jar")
						.addFile("hadoop-yarn-common-2.3.0.jar")
						.addFile("httpclient-4.3.3.jar")
						.addFile("httpcore-4.3.2.jar")
						.addFile("jackson-annotations-2.2.1.jar")
						.addFile("jackson-core-2.2.1.jar")
						.addFile("jackson-databind-2.2.1.jar")
						.addFile("jackson-jaxrs-base-2.2.1.jar")
						.addFile("jackson-jaxrs-json-provider-2.2.1.jar")
						.addFile("jackson-module-jaxb-annotations-2.2.1.jar")
						.addFile("javassist-3.12.1.GA.jar")
						.addFile("javax.inject-1.jar")
						.addFile("jaxrs-api-3.0.6.Final.jar")
						.addFile("jboss-annotations-api_1.1_spec-1.0.1.Final.jar")
						.addFile("jboss-annotations-api_1.2_spec-1.0.0.Alpha1.jar")
						.addFile("jboss-classfilewriter-1.0.4.Final.jar")
						.addFile("jboss-el-api_3.0_spec-1.0.0.Alpha1.jar")
						.addFile("jboss-interceptors-api_1.2_spec-1.0.0.Alpha3.jar")
						.addFile("jboss-logging-3.1.3.GA.jar")
						.addFile("jboss-servlet-api_3.1_spec-1.0.0.Final.jar")
						.addFile("jcip-annotations-1.0.jar")
						.addFile("log4j-1.2.17.jar")
						.addFile("protobuf-java-2.5.0.jar")
						.addFile("resteasy-cdi-3.0.6.Final.jar")
						.addFile("resteasy-client-3.0.6.Final.jar")
						.addFile("resteasy-jackson2-provider-3.0.6.Final.jar")
						.addFile("resteasy-jaxrs-3.0.6.Final.jar")
						.addFile("resteasy-undertow-3.0.6.Final.jar")
						.addFile("scannotation-1.0.3.jar")
						.addFile("slf4j-api-1.7.5.jar")
						.addFile("slf4j-log4j12-1.7.6.jar")
						.addFile("undertow-core-1.0.1.Final.jar")
						.addFile("undertow-servlet-1.0.1.Final.jar")
						.addFile("weld-api-2.1.Final.jar")
						.addFile("weld-core-impl-2.1.2.Final.jar")
						.addFile("weld-se-core-2.1.2.Final.jar")
						.addFile("weld-spi-2.1.Final.jar")
						.addFile("xnio-api-3.2.0.Final.jar")
						.addFile("xnio-nio-3.2.0.Final.jar")
						.build();
				ctx.setLocalResources(jars);
				
				// Setup CLASSPATH for ApplicationMaster
				Map<String, String> appMasterEnv = new HashMap<String, String>();
				setupAppMasterEnv(appMasterEnv);
				ctx.setEnvironment(appMasterEnv);
				
				logger.info("Launching container " + allocatedContainers);
				nmClient.startContainer(container, ctx);
			}
			Thread.sleep(100);
		}

		try {
			logger.info("Waiting for " + n + " containers to complete");
			int completedContainers = 0;
			while (completedContainers < n) {
				AllocateResponse response = rmClient.allocate(completedContainers
						/ n);
				for (ContainerStatus status : response
						.getCompletedContainersStatuses()) {
					++completedContainers;
					logger.info("Completed container " + completedContainers);
				}
				Thread.sleep(100);
			}
	
			logger.info("All containers completed, unregister with ResourceManager");
			rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED,
					"", "");
		} catch(Exception e) {
			logger.error("******** Application error ***********", e);
		}
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
