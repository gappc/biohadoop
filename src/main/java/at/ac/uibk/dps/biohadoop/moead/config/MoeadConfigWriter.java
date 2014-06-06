package at.ac.uibk.dps.biohadoop.moead.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.moead.master.socket.MoeadSocketServer;
import at.ac.uibk.dps.biohadoop.moead.worker.SocketMoeadWorker;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileLoadConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FilePersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileSaveConfiguration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MoeadConfigWriter {

	private static final Logger LOG = LoggerFactory
			.getLogger(MoeadConfigWriter.class);

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-moead";
	private static String LOCAL_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_OUTPUT_NAME = CONF_OUTPUT_DIR + "/"
			+ CONF_NAME + ".json";

	private static String LOCAL_PERSISTENCE_SAVE_PATH = "/tmp/biohadoop/moead";
	private static String REMOTE_PERSISTENCE_SAVE_PATH = "/biohadoop/persistence/moead";
	private static String LOCAL_PERSISTENCE_LOAD_PATH = "/tmp/biohadoop/moead/MOEAD-LOCAL-1/2087724778";
	private static String REMOTE_PERSISTENCE_LOAD_PATH = "/biohadoop/persistence/moead";

	private MoeadConfigWriter() {
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		BiohadoopConfiguration biohadoopConfig = new BiohadoopConfiguration();

		List<String> endpoints = Arrays.asList(MoeadSocketServer.class
				.getName());
		biohadoopConfig.setEndPoints(endpoints);

		biohadoopConfig.setIncludePaths(Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/"));

		biohadoopConfig.setVersion("0.1");

		Map<String, Integer> workers = new HashMap<String, Integer>();
		workers.put(SocketMoeadWorker.class.getName(), 3);
		biohadoopConfig.setWorkers(workers);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		ApplicationConfiguration applicationConfig = buildApplicationConfig(
				"MOEAD-LOCAL-1", true);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig,
				applicationConfig));
		mapper.writeValue(new File(LOCAL_OUTPUT_NAME), biohadoopConfig);

		applicationConfig = buildApplicationConfig("MOEAD-DISTRIBUTED-1", false);
		biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig,
				applicationConfig));
		mapper.writeValue(new File(REMOTE_OUTPUT_NAME), biohadoopConfig);

		readAlgorithmConfig();
	}

	private static ApplicationConfiguration buildApplicationConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);
		PersistenceConfiguration persistenceConfiguration = buildPersistenceConfig(local);

		return new ApplicationConfiguration(name, algorithmConfiguration,
				Moead.class, persistenceConfiguration);
	}

	private static AlgorithmConfiguration buildAlgorithmConfig(boolean local) {
		MoeadAlgorithmConfig moeadAlgorithmConfig = new MoeadAlgorithmConfig();
		if (local) {
			moeadAlgorithmConfig.setOutputFile("/tmp/moead-sol.txt");
		} else {
			moeadAlgorithmConfig.setOutputFile("/biohadoop/data/moead-sol.txt");
		}

		moeadAlgorithmConfig.setMaxIterations(1000);
		moeadAlgorithmConfig.setPopulationSize(300);
		moeadAlgorithmConfig.setNeighborSize(290);
		moeadAlgorithmConfig.setGenomeSize(10);

		return moeadAlgorithmConfig;
	}

	private static PersistenceConfiguration buildPersistenceConfig(boolean local) {
		String savePath = null;
		String loadPath = null;
		if (local) {
			savePath = LOCAL_PERSISTENCE_SAVE_PATH;
			loadPath = LOCAL_PERSISTENCE_LOAD_PATH;
		} else {
			savePath = REMOTE_PERSISTENCE_SAVE_PATH;
			loadPath = REMOTE_PERSISTENCE_LOAD_PATH;
		}

		FileSaveConfiguration saveFile = new FileSaveConfiguration(savePath,
				100);
		FileLoadConfiguration loadFile = new FileLoadConfiguration(loadPath,
				true);

		PersistenceConfiguration filePersistenceConfiguration = new FilePersistenceConfiguration(
				saveFile, loadFile);

		return filePersistenceConfiguration;
	}

	private static void readAlgorithmConfig() throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		BiohadoopConfiguration config = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		config = mapper.readValue(new File(LOCAL_OUTPUT_NAME),
				BiohadoopConfiguration.class);
		LOG.info(config.toString());

		config = mapper.readValue(new File(REMOTE_OUTPUT_NAME),
				BiohadoopConfiguration.class);
		LOG.info(config.toString());
	}
}
