package at.ac.uibk.dps.biohadoop.moead.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.moead.master.socket.MoeadSocketServer;
import at.ac.uibk.dps.biohadoop.moead.worker.SocketMoeadWorker;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MoeadConfigWriter {

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-moead";
	private static String LOCAL_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ ".json";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		BiohadoopConfiguration biohadoopConfig = new BiohadoopConfiguration();

		List<String> endpoints = Arrays.asList(MoeadSocketServer.class.getName());
		biohadoopConfig.setEndPoints(endpoints);

		biohadoopConfig.setIncludePaths(Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/"));

		biohadoopConfig.setVersion("0.1");

		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketMoeadWorker.class.getName(), 3);
		biohadoopConfig.setWorkers(workers);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		ApplicationConfiguration applicationConfig = buildApplicationConfig("MOEAD-LOCAL-1", true);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig, applicationConfig));
		mapper.writeValue(new File(LOCAL_OUTPUT_NAME), biohadoopConfig);

		applicationConfig = buildApplicationConfig("MOEAD-DISTRIBUTED-1", false);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig, applicationConfig));
		mapper.writeValue(new File(REMOTE_OUTPUT_NAME), biohadoopConfig);

		readAlgorithmConfig();
	}

	private static ApplicationConfiguration buildApplicationConfig(String name, boolean local) {
		MoeadAlgorithmConfig moeadAlgorithmConfig = new MoeadAlgorithmConfig();
		moeadAlgorithmConfig.setAlgorithm(Moead.class.getName());
		if (local) {
			moeadAlgorithmConfig.setOutputFile("/tmp/moead-sol.txt");
		} else {
			moeadAlgorithmConfig.setOutputFile("/biohadoop/data/moead-sol.txt");
		}
		
		moeadAlgorithmConfig.setMaxIterations(1000);
		moeadAlgorithmConfig.setPopulationSize(300);
		moeadAlgorithmConfig.setNeighborSize(290);
		moeadAlgorithmConfig.setGenomeSize(10);
		
		ApplicationConfiguration applicationConfig = new ApplicationConfiguration(name,
				moeadAlgorithmConfig, Moead.class);
		
		return applicationConfig;
	}

	private static void readAlgorithmConfig() throws JsonParseException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		BiohadoopConfiguration config = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		config = mapper.readValue(new File(LOCAL_OUTPUT_NAME), BiohadoopConfiguration.class);
		System.out.println(config);
		
		config = mapper.readValue(new File(REMOTE_OUTPUT_NAME), BiohadoopConfiguration.class);
		System.out.println(config);
	}
}
