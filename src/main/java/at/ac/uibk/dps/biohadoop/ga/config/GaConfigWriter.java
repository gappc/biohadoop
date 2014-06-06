package at.ac.uibk.dps.biohadoop.ga.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.kryo.GaKryoResource;
import at.ac.uibk.dps.biohadoop.ga.master.socket.GaSocketServer;
import at.ac.uibk.dps.biohadoop.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.server.UndertowServer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class GaConfigWriter {

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-ga";
	private static String LOCAL_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ ".json";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		BiohadoopConfiguration biohadoopConfig = new BiohadoopConfiguration();

		List<String> endpoints = Arrays.asList(GaSocketServer.class.getName(),
				GaKryoResource.class.getName(), UndertowServer.class.getName());
		biohadoopConfig.setEndPoints(endpoints);

		biohadoopConfig.setIncludePaths(Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/"));

		biohadoopConfig.setVersion("0.1");

		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketGaWorker.class.getName(), 3);
		biohadoopConfig.setWorkers(workers);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		ApplicationConfiguration applicationConfig = buildApplicationConfig("GA-LOCAL-1", true);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig, applicationConfig));
		mapper.writeValue(new File(LOCAL_OUTPUT_NAME), biohadoopConfig);

		applicationConfig = buildApplicationConfig("GA-DISTRIBUTED-1", false);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig, applicationConfig));
		mapper.writeValue(new File(REMOTE_OUTPUT_NAME), biohadoopConfig);

		readAlgorithmConfig();
	}

	private static ApplicationConfiguration buildApplicationConfig(String name, boolean local) {
		String dataFile = null;
		if (local) {
			dataFile = "/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp";
		} else {
			dataFile = "/biohadoop/data/att48.tsp";
		}

		GaAlgorithmConfig gaAlgorithmConfig = new GaAlgorithmConfig();
		gaAlgorithmConfig.setDataFile(dataFile);
		gaAlgorithmConfig.setMaxIterations(10000);
		gaAlgorithmConfig.setPopulationSize(10);
		
		ApplicationConfiguration applicationConfig = new ApplicationConfiguration(name,
				gaAlgorithmConfig, Ga.class);
		
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
