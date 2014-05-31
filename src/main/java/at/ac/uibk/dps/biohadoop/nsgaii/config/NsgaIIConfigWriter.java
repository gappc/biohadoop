package at.ac.uibk.dps.biohadoop.nsgaii.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.hadoop.Config;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.nsgaii.master.socket.NsgaIISocketServer;
import at.ac.uibk.dps.biohadoop.nsgaii.worker.SocketNsgaIIWorker;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NsgaIIConfigWriter {

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-nsgaii";
	private static String LOCAL_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ ".json";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

//		List<String> endpoints = Arrays.asList(NsgaIILocalResource.class.getName(), NsgaIISocketServer.class.getName());
		List<String> endpoints = Arrays.asList(NsgaIISocketServer.class.getName());
//		Map<String, Integer> workers = Collections.EMPTY_MAP;
		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketNsgaIIWorker.class.getName(), 3);
		NsgaIIConfig config = new NsgaIIConfig();
		config.setVersion("0.1");
		config.setMasterEndpoints(endpoints);
		config.setWorkers(workers);
		config.setLauncherClass(NsgaIILauncher.class.getName());
		config.setIncludePaths(Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/"));

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		config.setAlgorithmConfig(buildAlgorithmConfig(true));
		mapper.writeValue(new File(LOCAL_NAME), config);
		
		config.setAlgorithmConfig(buildAlgorithmConfig(false));
		mapper.writeValue(new File(REMOTE_NAME), config);
		readAlgorithmConfig();
	}

	private static NsgaIIAlgorithmConfig buildAlgorithmConfig(boolean local) {
		NsgaIIAlgorithmConfig config = new NsgaIIAlgorithmConfig();
		config.setAlgorithm(NsgaII.class.getName());
		if (local) {
			config.setOutputFile("/tmp/nsgaii-sol.txt");
		} else {
			config.setOutputFile("/biohadoop/data/nsgaii-sol.txt");
		}
		
		config.setMaxIterations(500);
		config.setPopulationSize(300);
		config.setGenomeSize(100);
		return config;
	}

	private static void readAlgorithmConfig() throws JsonParseException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Config config = (Config) Class.forName(NsgaIIConfig.class.getName())
				.newInstance();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
			config = mapper.readValue(new File(LOCAL_NAME), config.getClass());
			config = mapper.readValue(new File(REMOTE_NAME), config.getClass());

		System.out.println(config);
		System.out.println(config.getClass().getName());
	}
}
