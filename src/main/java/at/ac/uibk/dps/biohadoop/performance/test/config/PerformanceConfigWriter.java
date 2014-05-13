package at.ac.uibk.dps.biohadoop.performance.test.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.ga.config.GaConfig;
import at.ac.uibk.dps.biohadoop.hadoop.Config;
import at.ac.uibk.dps.biohadoop.performance.test.master.socket.PerformanceSocketServer;
import at.ac.uibk.dps.biohadoop.performance.test.worker.SocketPerformanceWorker;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PerformanceConfigWriter {

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-performance";
	private static String LOCAL_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ ".json";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		System.out.println(System.getProperty("local"));

		List<String> endpoints = Arrays.asList(PerformanceSocketServer.class
				.getName());
		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketPerformanceWorker.class.getName(), 2);
		PerformanceConfig config = new PerformanceConfig();
		config.setVersion("0.1");
		config.setMasterEndpoints(endpoints);
		config.setWorkers(workers);
		config.setLauncherClass(PerformanceLauncher.class.getName());
		config.setAlgorithmConfig(buildAlgorithmConfig());
		config.setIncludePaths(Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/"));

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(new File(LOCAL_NAME), config);
		mapper.writeValue(new File(REMOTE_NAME), config);
		readAlgorithmConfig();
	}

	private static PerformanceAlgorithmConfig buildAlgorithmConfig() {
		PerformanceAlgorithmConfig config = new PerformanceAlgorithmConfig();
		config.setMaxIterations(200000);
		config.setClientSleepMillis(0);
		return config;
	}

	private static void readAlgorithmConfig() throws JsonParseException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Config config = (Config) Class.forName(GaConfig.class.getName())
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
