package at.ac.uibk.dps.biohadoop.ga.config;

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
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.kryo.GaKryo;
import at.ac.uibk.dps.biohadoop.ga.master.rest.GaRest;
import at.ac.uibk.dps.biohadoop.ga.master.socket.GaSocket;
import at.ac.uibk.dps.biohadoop.ga.master.websocket.GaWebSocket;
import at.ac.uibk.dps.biohadoop.ga.worker.KryoGaWorker;
import at.ac.uibk.dps.biohadoop.ga.worker.RestGaWorker;
import at.ac.uibk.dps.biohadoop.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.ga.worker.WebSocketGaWorker;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileLoadConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FilePersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FileSaveConfiguration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class GaConfigWriter {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaConfigWriter.class);

	private static String CONF_OUTPUT_DIR = "/sdb/studium/master-thesis/code/git/masterthesis/conf";
	private static String CONF_NAME = "biohadoop-ga";
	private static String LOCAL_OUTPUT_NAME = CONF_OUTPUT_DIR + "/" + CONF_NAME
			+ "-local.json";
	private static String REMOTE_OUTPUT_NAME = CONF_OUTPUT_DIR + "/"
			+ CONF_NAME + ".json";

	private static String LOCAL_PERSISTENCE_SAVE_PATH = "/tmp/biohadoop/ga";
	private static String REMOTE_PERSISTENCE_SAVE_PATH = "/biohadoop/persistence/ga";
	private static String LOCAL_PERSISTENCE_LOAD_PATH = "/tmp/biohadoop/ga/GA-LOCAL-1/481038014/";
	private static String REMOTE_PERSISTENCE_LOAD_PATH = "/biohadoop/persistence/ga";

	private static String LOCAL_DISTRIBUTION_INFO_HOST = "localhost";
	private static int LOCAL_DISTRIBUTION_INFO_PORT = 2181;
	private static String REMOTE_DISTRIBUTION_INFO_HOST = "master";
	private static int REMOTE_DISTRIBUTION_INFO_PORT = 2181;

	private GaConfigWriter() {
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
				"GA-LOCAL-1", local);
		ConnectionConfiguration connectionConfiguration = buildConnectionConfiguration();
		DistributionConfiguration distributionConfiguration = buildDistributionConfig(local);

		return new BiohadoopConfiguration(version, includePaths, Arrays.asList(
				applicationConfig, applicationConfig, applicationConfig,
				applicationConfig), connectionConfiguration,
				distributionConfiguration);
	}

	private static ConnectionConfiguration buildConnectionConfiguration() {
		List<Class<? extends MasterConnection>> endpoints = new ArrayList<>();
		endpoints.add(GaSocket.class);
		endpoints.add(GaKryo.class);
		endpoints.add(GaRest.class);
		endpoints.add(GaWebSocket.class);

		FileMasterConfiguration mc = new FileMasterConfiguration(endpoints);

		List<FileMasterConfiguration> masters = new ArrayList<>();
		masters.add(mc);

		Map<String, Integer> workers = new HashMap<>();
		workers.put(SocketGaWorker.class.getCanonicalName(), 3);
		workers.put(KryoGaWorker.class.getCanonicalName(), 1);
		workers.put(RestGaWorker.class.getCanonicalName(), 1);
		workers.put(WebSocketGaWorker.class.getCanonicalName(), 1);

		return new ConnectionConfiguration(masters, workers);
	}

	private static ApplicationConfiguration buildApplicationConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);
		PersistenceConfiguration persistenceConfiguration = buildPersistenceConfig(local);

		return new ApplicationConfiguration(name, algorithmConfiguration,
				Ga.class, persistenceConfiguration);
	}

	private static AlgorithmConfiguration buildAlgorithmConfig(boolean local) {
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

		return gaAlgorithmConfig;
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
