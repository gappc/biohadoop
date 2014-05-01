package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.ArgumentChecker;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class Client {

	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

	public static void main(String[] args) throws Exception {
		LOGGER.info("BIOHADOOP started at " + Hostname.getHostname());
		long start = System.currentTimeMillis();
		
		if (ArgumentChecker.checkArgs(args)) {
			YarnConfiguration conf = new YarnConfiguration();
			Client c = new Client();
			c.run(conf, args);
		}
		
		long end = System.currentTimeMillis();
		LOGGER.info("Time: " + (end - start) + "ms");
	}

//	TODO change, that it is runnable from main but also from oozie
	public void run(YarnConfiguration conf, String[] args) throws Exception {
		LOGGER.info("############ Starting client ############");

		final String algorithm = args[0];
		final String containerCount = args[1];

		// Configure yarnClient
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
				+ " at.ac.uibk.dps.biohadoop.hadoop.ApplicationMaster "
				+ algorithm + " " + containerCount + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
				+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stderr"));

		// Set libs
		String libPath = "hdfs://" + Hostname.getHostname() + ":54310/biohadoop/lib/";
		String dataPath = "hdfs://" + Hostname.getHostname() + ":54310/biohadoop/data/";
		Map<String, LocalResource> jars = LocalResourceBuilder.getStandardResources(libPath, conf);
		Map<String, LocalResource> data = LocalResourceBuilder.getStandardResources(dataPath, conf);
		Map<String, LocalResource> combinedFiles = new HashMap<String, LocalResource>();
		combinedFiles.putAll(jars);
		combinedFiles.putAll(data);
		amContainer.setLocalResources(combinedFiles);

		// Setup CLASSPATH for ApplicationMaster
		Map<String, String> appMasterEnv = new HashMap<String, String>();
		setupAppMasterEnv(appMasterEnv, conf);
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
		LOGGER.info("Submitting application " + appId);
		yarnClient.submitApplication(appContext);

		ApplicationReport appReport = yarnClient.getApplicationReport(appId);
		YarnApplicationState appState = appReport.getYarnApplicationState();
		
		LOGGER.info("Tracking URL: " + appReport.getTrackingUrl());
		LOGGER.info("Application Master running at: " + appReport.getHost());
		
		while (appState != YarnApplicationState.FINISHED
				&& appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED) {
			Thread.sleep(100);
			appReport = yarnClient.getApplicationReport(appId);
			appState = appReport.getYarnApplicationState();
			LOGGER.info("Progress: " + appReport.getProgress());
		}
		
		LOGGER.info("Application " + appId + " finished with"
				+ " state " + appState + " at " + appReport.getFinishTime());
	}

	private void setupAppMasterEnv(Map<String, String> appMasterEnv, Configuration conf) {
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
