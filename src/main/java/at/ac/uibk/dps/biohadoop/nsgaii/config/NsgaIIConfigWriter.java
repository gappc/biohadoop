package at.ac.uibk.dps.biohadoop.nsgaii.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.connection.FileMasterConfiguration;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.nsgaii.master.socket.NsgaIISocket;
import at.ac.uibk.dps.biohadoop.nsgaii.worker.SocketNsgaIIWorker;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileLoadConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FilePersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileSaveConfiguration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NsgaIIConfigWriter {

	private static final Logger LOG = LoggerFactory
			.getLogger(NsgaIIConfigWriter.class);

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-nsgaii";
	private static String LOCAL_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_OUTPUT_NAME = CONF_OUTPUT_DIR + "/"
			+ CONF_NAME + ".json";

	private static String LOCAL_PERSISTENCE_SAVE_PATH = "/tmp/biohadoop/nsgaii";
	private static String REMOTE_PERSISTENCE_SAVE_PATH = "/biohadoop/persistence/nsgaii";
	private static String LOCAL_PERSISTENCE_LOAD_PATH = "/tmp/biohadoop/nsgaii/NSGAII-LOCAL-1/1263479909";
	private static String REMOTE_PERSISTENCE_LOAD_PATH = "/biohadoop/persistence/nsgaii";

	private static String LOCAL_DISTRIBUTION_INFO_HOST = "localhost";
	private static int LOCAL_DISTRIBUTION_INFO_PORT = 2181;
	private static String REMOTE_DISTRIBUTION_INFO_HOST = "master";
	private static int REMOTE_DISTRIBUTION_INFO_PORT = 2181;
	
	private NsgaIIConfigWriter() {
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		BiohadoopConfiguration biohadoopConfig = buildBiohadoopConfiguration(true);
		mapper.writeValue(new File(LOCAL_OUTPUT_NAME), biohadoopConfig);

		biohadoopConfig = buildBiohadoopConfiguration(false);
		mapper.writeValue(new File(REMOTE_OUTPUT_NAME), biohadoopConfig);

		readAlgorithmConfig();
	}

	private static BiohadoopConfiguration buildBiohadoopConfiguration(
			boolean local) {
		String version = "0.1";
		List<String> includePaths = Arrays.asList("/biohadoop/lib/",
				"/biohadoop/conf/");
		ApplicationConfiguration applicationConfig = buildApplicationConfig(
				"MOEAD-LOCAL-1", local);
		ConnectionConfiguration connectionConfiguration = buildConnectionConfiguration();
		DistributionConfiguration distributionConfiguration = buildDistributionConfig(local);

		return new BiohadoopConfiguration(version, includePaths, Arrays.asList(
				applicationConfig, applicationConfig, applicationConfig,
				applicationConfig), connectionConfiguration,
				distributionConfiguration);
	}

	private static ConnectionConfiguration buildConnectionConfiguration() {
		List<Class<? extends MasterConnection>> endpoints = new ArrayList<>();
		endpoints.add(NsgaIISocket.class);

		FileMasterConfiguration mc = new FileMasterConfiguration(endpoints);

		List<FileMasterConfiguration> masters = new ArrayList<>();
		masters.add(mc);

		Map<String, Integer> workers = new HashMap<>();
		workers.put(SocketNsgaIIWorker.class.getCanonicalName(), 3);

		return new ConnectionConfiguration(masters, workers);
	}

	private static ApplicationConfiguration buildApplicationConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);
		PersistenceConfiguration persistenceConfiguration = buildPersistenceConfig(local);

		return new ApplicationConfiguration(name, algorithmConfiguration,
				Moead.class, persistenceConfiguration);
	}

	private static AlgorithmConfiguration buildAlgorithmConfig(boolean local) {
		NsgaIIAlgorithmConfig nsgaIIConfig = new NsgaIIAlgorithmConfig();
		nsgaIIConfig.setAlgorithm(NsgaII.class.getName());
		if (local) {
			nsgaIIConfig.setOutputFile("/tmp/nsgaii-sol.txt");
		} else {
			nsgaIIConfig.setOutputFile("/biohadoop/data/nsgaii-sol.txt");
		}

		nsgaIIConfig.setMaxIterations(500);
		nsgaIIConfig.setPopulationSize(300);
		nsgaIIConfig.setGenomeSize(100);

		return nsgaIIConfig;
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
				1000);
		FileLoadConfiguration loadFile = new FileLoadConfiguration(loadPath,
				true);

		PersistenceConfiguration filePersistenceConfiguration = new FilePersistenceConfiguration(
				saveFile, loadFile);

		return filePersistenceConfiguration;
	}

	private static DistributionConfiguration buildDistributionConfig(
			boolean local) {
		if (local) {
			return new DistributionConfiguration(LOCAL_DISTRIBUTION_INFO_HOST,
					LOCAL_DISTRIBUTION_INFO_PORT);
		} else {
			return new DistributionConfiguration(REMOTE_DISTRIBUTION_INFO_HOST,
					REMOTE_DISTRIBUTION_INFO_PORT);
		}
	}

	// public static void main(String[] args) throws IOException,
	// ClassNotFoundException, InstantiationException,
	// IllegalAccessException {
	//
	// BiohadoopConfiguration biohadoopConfig = new BiohadoopConfiguration();
	//
	// List<String> endpoints = Arrays.asList(NsgaIISocketServer.class
	// .getName());
	// biohadoopConfig.setEndPoints(endpoints);
	//
	// biohadoopConfig.setIncludePaths(Arrays.asList("/biohadoop/lib/",
	// "/biohadoop/conf/"));
	//
	// biohadoopConfig.setVersion("0.1");
	//
	// Map<String, Integer> workers = new HashMap<String, Integer>();
	// workers.put(SocketNsgaIIWorker.class.getName(), 3);
	// biohadoopConfig.setWorkers(workers);
	//
	// ObjectMapper mapper = new ObjectMapper();
	// mapper.enable(SerializationFeature.INDENT_OUTPUT);
	//
	// ApplicationConfiguration applicationConfig = buildApplicationConfig(
	// "NSGAII-LOCAL-1", true);
	// biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig,
	// applicationConfig));
	// mapper.writeValue(new File(LOCAL_OUTPUT_NAME), biohadoopConfig);
	//
	// applicationConfig = buildApplicationConfig("NSGAII-DISTRIBUTED-1",
	// false);
	// biohadoopConfig.setApplicationConfigs(Arrays.asList(applicationConfig,
	// applicationConfig));
	// mapper.writeValue(new File(REMOTE_OUTPUT_NAME), biohadoopConfig);
	//
	// readAlgorithmConfig();
	// }
	//
	// private static ApplicationConfiguration buildApplicationConfig(String
	// name,
	// boolean local) {
	// AlgorithmConfiguration algorithmConfiguration =
	// buildAlgorithmConfig(local);
	// PersistenceConfiguration persistenceConfiguration =
	// buildPersistenceConfig(local);
	//
	// return new ApplicationConfiguration(name, algorithmConfiguration,
	// NsgaII.class, persistenceConfiguration);
	// }
	//
	// private static AlgorithmConfiguration buildAlgorithmConfig(boolean local)
	// {
	// NsgaIIAlgorithmConfig nsgaIIConfig = new NsgaIIAlgorithmConfig();
	// nsgaIIConfig.setAlgorithm(NsgaII.class.getName());
	// if (local) {
	// nsgaIIConfig.setOutputFile("/tmp/nsgaii-sol.txt");
	// } else {
	// nsgaIIConfig.setOutputFile("/biohadoop/data/nsgaii-sol.txt");
	// }
	//
	// nsgaIIConfig.setMaxIterations(500);
	// nsgaIIConfig.setPopulationSize(300);
	// nsgaIIConfig.setGenomeSize(100);
	//
	// return nsgaIIConfig;
	// }
	//
	// private static PersistenceConfiguration buildPersistenceConfig(boolean
	// local) {
	// String savePath = null;
	// String loadPath = null;
	// if (local) {
	// savePath = LOCAL_PERSISTENCE_SAVE_PATH;
	// loadPath = LOCAL_PERSISTENCE_LOAD_PATH;
	// } else {
	// savePath = REMOTE_PERSISTENCE_SAVE_PATH;
	// loadPath = REMOTE_PERSISTENCE_LOAD_PATH;
	// }
	//
	// FileSaveConfiguration saveFile = new FileSaveConfiguration(savePath,
	// 100);
	// FileLoadConfiguration loadFile = new FileLoadConfiguration(loadPath,
	// true);
	//
	// PersistenceConfiguration filePersistenceConfiguration = new
	// FilePersistenceConfiguration(
	// saveFile, loadFile);
	//
	// return filePersistenceConfiguration;
	// }

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
