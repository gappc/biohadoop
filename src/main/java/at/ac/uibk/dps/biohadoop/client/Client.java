package at.ac.uibk.dps.biohadoop.client;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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

import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LocalResourceBuilder;

public class Client {

	private static Logger logger = LoggerFactory.getLogger(Client.class);

	Configuration conf = new YarnConfiguration();

	public static void main(String[] args) throws Exception {
		logger.info("BIOHADOOP started at " + Hostname.getHostname());
		long start = System.currentTimeMillis();
		Client c = new Client();
		c.run(args);
		long end = System.currentTimeMillis();
		logger.info("Time: " + (end - start) + "ms");
	}

	public void run(String[] args) throws Exception {
		logger.info("CLIENT CLASSPATH: " + System.getProperty("java.class.path"));
		
		logger.info("############ Starting client ############");

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
				+ " at.ac.uibk.dps.biohadoop.master.ApplicationMaster "
				+ command + " " + String.valueOf(n) + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
				+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stderr"));

		String libPath = "hdfs://" + Hostname.getHostname() + ":54310/biohadoop/lib/";
		
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
		amContainer.setLocalResources(jars);

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
		logger.info("Submitting application " + appId);
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

		logger.info("Application " + appId + " finished with"
				+ " state " + appState + " at " + appReport.getFinishTime());

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
