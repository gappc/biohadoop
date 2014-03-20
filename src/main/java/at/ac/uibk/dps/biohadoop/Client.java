package at.ac.uibk.dps.biohadoop;

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
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
	
	private static Logger logger = LoggerFactory.getLogger(Client.class);

	Configuration conf = new YarnConfiguration();

	public void run(String[] args) throws Exception {
		logger.error("############ Starting client ############");
		logger.info("############ Starting client ############");
		logger.debug("############ Starting client ############");
		
		final String command = args[0];
		final int n = Integer.valueOf(args[1]);
		final Path jarPath = new Path(args[2]);

		// Create yarnClient
		YarnConfiguration conf = new YarnConfiguration();
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		yarnClient.start();

		// Create application via yarnClient
		YarnClientApplication app = yarnClient.createApplication();

		// Set up the container launch context for the application master
		ContainerLaunchContext amContainer = Records
				.newRecord(ContainerLaunchContext.class);
		amContainer.setCommands(Collections.singletonList("$JAVA_HOME/bin/java"
				+ " -Xmx256M"
				+ " at.ac.uibk.dps.biohadoop.ApplicationMaster" + " "
				+ command + " " + String.valueOf(n) + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
				+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stderr"));

		// Setup jars for ApplicationMaster
		LocalResource appMasterJar = Records.newRecord(LocalResource.class);
		setupAppMasterJar(jarPath, appMasterJar);
		
		Path weldPath = new Path("hdfs://master:54310/biohadoop/weld-se-1.1.10.Final.jar");
		LocalResource weldJar = Records.newRecord(LocalResource.class);
		setupAppMasterJar(weldPath, weldJar);
		
		Map<String, LocalResource> jars = new HashMap<String, LocalResource>();
		jars.put("biohadoop.jar", appMasterJar);
		jars.put("weld.jar", weldJar);
		amContainer.setLocalResources(jars);
		
//		amContainer.setLocalResources(Collections.singletonMap("biohadoop.jar",
//				appMasterJar));

		// Setup CLASSPATH for ApplicationMaster
		Map<String, String> appMasterEnv = new HashMap<String, String>();
		setupAppMasterEnv(appMasterEnv);
		amContainer.setEnvironment(appMasterEnv);

		// Set up resource type requirements for ApplicationMaster
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(256);
		capability.setVirtualCores(1);

		// Finally, set-up ApplicationSubmissionContext for the application
		ApplicationSubmissionContext appContext = app
				.getApplicationSubmissionContext();
		appContext.setApplicationName("biohadoop"); // application name
		appContext.setAMContainerSpec(amContainer);
		appContext.setResource(capability);
		appContext.setQueue("default"); // queue

		// Submit application
		ApplicationId appId = appContext.getApplicationId();
		System.out.println("Submitting application " + appId);
		yarnClient.submitApplication(appContext);

		ApplicationReport appReport = yarnClient.getApplicationReport(appId);
		YarnApplicationState appState = appReport.getYarnApplicationState();
		while (appState != YarnApplicationState.FINISHED
				&& appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED) {
			Thread.sleep(100);
			appReport = yarnClient.getApplicationReport(appId);
			appState = appReport.getYarnApplicationState();
		}

		System.out.println("Application " + appId + " finished with"
				+ " state " + appState + " at " + appReport.getFinishTime());

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

	public static void main(String[] args) throws Exception {
		Client c = new Client();
		c.run(args);
	}
}
