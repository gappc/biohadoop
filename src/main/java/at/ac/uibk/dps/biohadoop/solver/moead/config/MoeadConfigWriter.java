package at.ac.uibk.dps.biohadoop.solver.moead.config;

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
import at.ac.uibk.dps.biohadoop.solver.ga.distribution.GaSimpleMerger;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.solver.moead.master.MoeadKryo;
import at.ac.uibk.dps.biohadoop.solver.moead.master.MoeadRest;
import at.ac.uibk.dps.biohadoop.solver.moead.master.MoeadWebSocket;
import at.ac.uibk.dps.biohadoop.solver.moead.master.socket.MoeadSocket;
import at.ac.uibk.dps.biohadoop.solver.moead.worker.SocketMoeadWorker;
import at.ac.uibk.dps.biohadoop.solver.moead.worker.WebSocketMoeadWorker;

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

	private static String LOCAL_DISTRIBUTION_INFO_HOST = "localhost";
	private static int LOCAL_DISTRIBUTION_INFO_PORT = 2181;
	private static String REMOTE_DISTRIBUTION_INFO_HOST = "master";
	private static int REMOTE_DISTRIBUTION_INFO_PORT = 2181;

	private MoeadConfigWriter() {
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
		SolverConfiguration solverConfig = buildSolverConfig("MOEAD-LOCAL-1",
				local);
		ConnectionConfiguration connectionConfiguration = buildConnectionConfiguration();
		GlobalDistributionConfiguration globalDistributionConfiguration = buildGlobalDistributionConfig(local);

		return new BiohadoopConfiguration(version, includePaths, Arrays.asList(
				solverConfig, solverConfig, solverConfig, solverConfig),
				connectionConfiguration, globalDistributionConfiguration);
	}

	private static ConnectionConfiguration buildConnectionConfiguration() {
		List<Class<? extends MasterConnection>> masterEndpoints = new ArrayList<>();
		masterEndpoints.add(MoeadSocket.class);
		masterEndpoints.add(MoeadWebSocket.class);
		masterEndpoints.add(MoeadRest.class);
		masterEndpoints.add(MoeadKryo.class);

		Map<String, Integer> workerEndpoints = new HashMap<>();
		workerEndpoints.put(SocketMoeadWorker.class.getCanonicalName(), 3);
		workerEndpoints.put(WebSocketMoeadWorker.class.getCanonicalName(), 3);

		return new ConnectionConfiguration(masterEndpoints, workerEndpoints);
	}

	private static SolverConfiguration buildSolverConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);
		PersistenceConfiguration persistenceConfiguration = buildPersistenceConfig(local);
		DistributionConfiguration distributionConfiguration = buildDistributionConfig();

		return new SolverConfiguration(name, algorithmConfiguration,
				Moead.class, persistenceConfiguration,
				distributionConfiguration);
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
