package at.ac.uibk.dps.biohadoop.ga.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.kryo.GaKryoResource;
import at.ac.uibk.dps.biohadoop.ga.master.socket.GaSocketServer;
import at.ac.uibk.dps.biohadoop.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.hadoop.Config;
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
	private static String LOCAL_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ ".json";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		System.out.println(System.getProperty("local"));

//		 List<String> endpoints =
//		 Arrays.asList(GaSocketServer.class.getName(),
//		 GaKryoResource.class.getName(), UndertowServer.class.getName(),
//		 GaLocalResource.class.getName());
		List<String> endpoints = Arrays.asList(GaSocketServer.class.getName(),
				GaKryoResource.class.getName(), UndertowServer.class.getName());
		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketGaWorker.class.getName(), 3);
		GaConfig config = new GaConfig();
		config.setVersion("0.1");
		config.setMasterEndpoints(endpoints);
		config.setWorkers(workers);
		config.setLauncherClass(GaLauncher.class.getName());
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

	private static GaAlgorithmConfig buildAlgorithmConfig(boolean local) {
		GaAlgorithmConfig config = new GaAlgorithmConfig();
		config.setAlgorithm(Ga.class.getName());
		if (local) {
			config.setDataFile("/sdb/studium/master-thesis/code/git/masterthesis/data/att48.tsp");
		} else {
			config.setDataFile("/biohadoop/data/att48.tsp");
		}
		config.setMaxIterations(10000);
		config.setPopulationSize(10);
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
		// ObjectMapper mapper = new ObjectMapper();
		// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
		// false);
		// AbstractBiohadoopConfig config = mapper.readValue(new File(
		// "/tmp/biohadoop-ga.json"), AbstractBiohadoopConfig.class);
		// System.out.println(config.getConfigType());
		// System.out.println(((GaConfig)config).getWorkers());
	}
}
