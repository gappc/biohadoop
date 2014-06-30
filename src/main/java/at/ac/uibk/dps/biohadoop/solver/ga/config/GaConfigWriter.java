package at.ac.uibk.dps.biohadoop.solver.ga.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.service.distribution.DistributionConfiguration;
import at.ac.uibk.dps.biohadoop.service.distribution.GlobalDistributionConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.file.FileLoadConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.file.FilePersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.file.FileSaveConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.solver.ga.distribution.GaSimpleMerger;
import at.ac.uibk.dps.biohadoop.solver.ga.master.kryo.GaKryo;
import at.ac.uibk.dps.biohadoop.solver.ga.master.rest.GaRest;
import at.ac.uibk.dps.biohadoop.solver.ga.master.socket.GaSocket;
import at.ac.uibk.dps.biohadoop.solver.ga.master.websocket.GaWebSocket;
import at.ac.uibk.dps.biohadoop.solver.ga.worker.KryoGaWorker;
import at.ac.uibk.dps.biohadoop.solver.ga.worker.RestGaWorker;
import at.ac.uibk.dps.biohadoop.solver.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.solver.ga.worker.WebSocketGaWorker;

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
		SolverConfiguration solverConfig = buildSolverConfig("GA-LOCAL-1",
				local);
		ConnectionConfiguration connectionConfiguration = buildConnectionConfiguration();
		GlobalDistributionConfiguration globalDistributionConfiguration = buildGlobalDistributionConfig(local);

		return new BiohadoopConfiguration(version, includePaths, Arrays.asList(
				solverConfig, solverConfig, solverConfig, solverConfig),
				connectionConfiguration, globalDistributionConfiguration);
	}

	private static ConnectionConfiguration buildConnectionConfiguration() {
		List<Class<? extends MasterConnection>> masterEndpoints = new ArrayList<>();
		masterEndpoints.add(GaSocket.class);
		masterEndpoints.add(GaKryo.class);
		masterEndpoints.add(GaRest.class);
		masterEndpoints.add(GaWebSocket.class);

		Map<String, Integer> workerEndpoints = new HashMap<>();
		workerEndpoints.put(SocketGaWorker.class.getCanonicalName(), 3);
		workerEndpoints.put(KryoGaWorker.class.getCanonicalName(), 1);
		workerEndpoints.put(RestGaWorker.class.getCanonicalName(), 1);
		workerEndpoints.put(WebSocketGaWorker.class.getCanonicalName(), 1);

		return new ConnectionConfiguration(masterEndpoints, workerEndpoints);
	}

	private static SolverConfiguration buildSolverConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);
		PersistenceConfiguration persistenceConfiguration = buildPersistenceConfig(local);
		DistributionConfiguration distributionConfiguration = buildDistributionConfig();

		return new SolverConfiguration(name, algorithmConfiguration, Ga.class,
				persistenceConfiguration, distributionConfiguration);
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
		gaAlgorithmConfig.setMaxIterations(1000);
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

	private static DistributionConfiguration buildDistributionConfig() {
		return new DistributionConfiguration(GaSimpleMerger.class, 2000);
	}

	private static GlobalDistributionConfiguration buildGlobalDistributionConfig(
			boolean local) {
		if (local) {
			return new GlobalDistributionConfiguration(
					LOCAL_DISTRIBUTION_INFO_HOST, LOCAL_DISTRIBUTION_INFO_PORT);
		} else {
			return new GlobalDistributionConfiguration(
					REMOTE_DISTRIBUTION_INFO_HOST,
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