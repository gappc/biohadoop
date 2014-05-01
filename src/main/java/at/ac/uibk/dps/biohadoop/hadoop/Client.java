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
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class Client {

	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

	public static void main(String[] args) {
		LOGGER.info("Client started at " + Hostname.getHostname());
		long start = System.currentTimeMillis();

		Client c = new Client();
		c.run(args, new YarnConfiguration());

		long end = System.currentTimeMillis();
		LOGGER.info("Client stopped, time: {}ms", end - start);
	}

	public void run(String[] args, YarnConfiguration conf) {
		if (!checkArguments(conf, args)) {
			return;
		}
		try {
			startApplicationMaster(conf, args[0]);
		} catch (Exception e) {
			LOGGER.error("Error while executing Client", e);
		}
	}

	private boolean checkArguments(YarnConfiguration conf, String[] args) {
		LOGGER.info("Checking arguments");
		if (!ArgumentChecker.isArgumentCountValid(args, 1)) {
			return false;
		}
		if (!HdfsUtil.fileExists(conf, args[0])) {
			return false;
		}
		return true;
	}

	private void startApplicationMaster(YarnConfiguration yarnConfiguration,
			String configFilename) throws Exception {
		LOGGER.info("Launching Application Master");

		// Configure yarnClient
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(yarnConfiguration);
		yarnClient.start();

		// Create application via yarnClient
		YarnClientApplication app = yarnClient.createApplication();

		// Set up the container launch context for the application master
		ContainerLaunchContext amContainer = Records
				.newRecord(ContainerLaunchContext.class);

		String launchCommand = "$JAVA_HOME/bin/java" + " -Xmx256M "
				+ ApplicationMaster.class.getName() + " " + configFilename + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
				+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stderr";
		LOGGER.info("Launch command: {}", launchCommand);

		amContainer.setCommands(Collections.singletonList(launchCommand));

		// Set libs
//		Map<String, LocalResource> combinedFiles = new HashMap<String, LocalResource>();
//		String defaultFs = yarnConfiguration.get("fs.defaultFS");
//		Launcher launcher = LaunchBuilder.buildLauncher(yarnConfiguration, configFilename);
//		for (String includePath : launcher.getConfiguration(configFilename).getIncludePaths()) {
//			Map<String, LocalResource> includes = LocalResourceBuilder
//					.getStandardResources(defaultFs + includePath, yarnConfiguration);
//			combinedFiles.putAll(includes);
//		}
//		amContainer.setLocalResources(combinedFiles);
		
		String libPath = "hdfs://" + Hostname.getHostname()
				+ ":54310/biohadoop/lib/";
		String dataPath = "hdfs://" + Hostname.getHostname()
				+ ":54310/biohadoop/data/";
		String confPath = "hdfs://" + Hostname.getHostname()
				+ ":54310/biohadoop/conf/";
		Map<String, LocalResource> jars = LocalResourceBuilder
				.getStandardResources(libPath, yarnConfiguration);
		Map<String, LocalResource> data = LocalResourceBuilder
				.getStandardResources(dataPath, yarnConfiguration);
		Map<String, LocalResource> conf = LocalResourceBuilder
				.getStandardResources(confPath, yarnConfiguration);
		Map<String, LocalResource> combinedFiles = new HashMap<String, LocalResource>();
		combinedFiles.putAll(jars);
		combinedFiles.putAll(data);
		combinedFiles.putAll(conf);
		amContainer.setLocalResources(combinedFiles);

		// Setup CLASSPATH for ApplicationMaster
		Map<String, String> appMasterEnv = new HashMap<String, String>();
		setupAppMasterEnv(appMasterEnv, yarnConfiguration);
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

		LOGGER.info("Tracking URL: {}", appReport.getTrackingUrl());
		LOGGER.info("Application Master running at: {}", appReport.getHost());

		while (appState != YarnApplicationState.FINISHED
				&& appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED) {
			Thread.sleep(100);
			appReport = yarnClient.getApplicationReport(appId);
			appState = appReport.getYarnApplicationState();
			LOGGER.info("Progress: {}", appReport.getProgress());
		}

		LOGGER.info("Application {} finished with state {} at {}", appId,
				appState, appReport.getFinishTime());
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
