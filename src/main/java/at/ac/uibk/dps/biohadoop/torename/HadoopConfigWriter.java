package at.ac.uibk.dps.biohadoop.torename;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.config.GaAlgorithmConfig;
import at.ac.uibk.dps.biohadoop.ga.config.GaConfig;
import at.ac.uibk.dps.biohadoop.ga.config.GaLauncher;
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

public class HadoopConfigWriter {

	private static boolean local = false;
	private static String confOutputDir = "/sdb/studium/master-thesis/code/git/masterthesis/conf";

	public static void main(String[] args) throws JsonGenerationException,
			JsonMappingException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		System.out.println(System.getProperty("local"));

//		List<String> endpoints = Arrays.asList(GaSocketServer.class.getName(),
//				GaKryoResource.class.getName(), UndertowServer.class.getName(),
//				GaLocalResource.class.getName());
		List<String> endpoints = Arrays.asList(GaSocketServer.class.getName(),
				GaKryoResource.class.getName(), UndertowServer.class.getName());
		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketGaWorker.class.getName(), 2);
		GaConfig config = new GaConfig();
		config.setVersion("0.1");
		config.setMasterEndpoints(endpoints);
		config.setWorkers(workers);
		config.setLauncherClass(GaLauncher.class.getName());
		config.setAlgorithmConfig(buildAlgorithmConfig());
		config.setIncludePaths(Arrays.asList("/biohadoop/lib/", "/biohadoop/conf/"));

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		if (local) {
			mapper.writeValue(new File(confOutputDir + "/biohadoop-ga-local.json"), config);
		} else {
			mapper.writeValue(new File(confOutputDir + "/biohadoop-ga.json"), config);
		}
		readAlgorithmConfig();
	}

	private static GaAlgorithmConfig buildAlgorithmConfig() {
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
		if (local) {
			config = mapper.readValue(new File(confOutputDir + "/biohadoop-ga-local.json"),
					config.getClass());
		}
		else {
			config = mapper.readValue(new File(confOutputDir + "/biohadoop-ga.json"),
					config.getClass());
		}

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
