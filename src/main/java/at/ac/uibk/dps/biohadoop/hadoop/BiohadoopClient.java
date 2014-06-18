package at.ac.uibk.dps.biohadoop.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.ArgumentChecker;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class BiohadoopClient {

	private static final Logger LOG = LoggerFactory.getLogger(BiohadoopClient.class);
	
	private List<String> includePaths = new ArrayList<String>();

	public static void main(String[] args) {
		LOG.info("Client started at " + HostInfo.getHostname());
		long start = System.currentTimeMillis();

		BiohadoopClient c = new BiohadoopClient();
		c.run(new YarnConfiguration(), args);

		long end = System.currentTimeMillis();
		LOG.info("Client stopped, time: {}ms", end - start);
	}

	public void run(YarnConfiguration conf, String[] args) {
		if (!checkArguments(conf, args)) {
			return;
		}
		try {
			startApplicationMaster(conf, args[0]);
		} catch (Exception e) {
			LOG.error("Error while executing Client", e);
		}
	}

	private boolean checkArguments(YarnConfiguration yarnConfiguration, String[] args) {
		LOG.info("Checking arguments");
		if (!ArgumentChecker.isArgumentCountValid(args, 1)) {
			return false;
		}
		if (!HdfsUtil.exists(yarnConfiguration, args[0])) {
			return false;
		}
		try {
			InputStream is = HdfsUtil
					.openFile(yarnConfiguration, args[0]);
			Reader reader = new BufferedReader(new InputStreamReader(is));
			
			@SuppressWarnings("rawtypes")
			Map jsonConfigAsMap = (Map) JSON.parse(reader);
			for (Object o : (Object[])jsonConfigAsMap.get("includePaths")) {
				String includePath = o.toString();
				
				LOG.info("Including includePath {}", includePath);
				if (!HdfsUtil.exists(yarnConfiguration, includePath)) {
					LOG.error("Could not find includePath {}", includePath);
					return false;
				}
				includePaths.add(includePath);
			}
		} catch (Exception e) {
			LOG.error("Error while checking if includePaths paths are available", e);
		}
		return true;
	}

	private void startApplicationMaster(YarnConfiguration yarnConfiguration,
			String configFilename) throws Exception {
		LOG.info("Launching Application Master");

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
				+ BiohadoopApplicationMaster.class.getName() + " " + configFilename
				+ " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stdout" + " 2>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";
		LOG.info("Launch command: {}", launchCommand);

		amContainer.setCommands(Collections.singletonList(launchCommand));

		Map<String, LocalResource> combinedFiles = new HashMap<String, LocalResource>();
		String defaultFs = yarnConfiguration.get("fs.defaultFS");
		for (String includePath : includePaths) {
			Map<String, LocalResource> includes = LocalResourceBuilder
					.getStandardResources(defaultFs + includePath, yarnConfiguration);
			combinedFiles.putAll(includes);
		}
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
		LOG.info("Submitting application " + appId);
		yarnClient.submitApplication(appContext);

		ApplicationReport appReport = yarnClient.getApplicationReport(appId);
		YarnApplicationState appState = appReport.getYarnApplicationState();

		LOG.info("Tracking URL: {}", appReport.getTrackingUrl());
		LOG.info("Application Master running at: {}", appReport.getHost());

		int count = 0;
		while (appState != YarnApplicationState.FINISHED
				&& appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED) {
			Thread.sleep(100);
			appReport = yarnClient.getApplicationReport(appId);
			appState = appReport.getYarnApplicationState();
			if (count++ % 20 == 0) {
				LOG.info("Progress: {}", appReport.getProgress());
			}
		}

		LOG.info("Application {} finished with state {} at {}", appId,
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
