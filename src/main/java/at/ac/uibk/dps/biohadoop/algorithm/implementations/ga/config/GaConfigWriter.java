package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaKryo;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaLocal;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaRest;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaSocket;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaWebSocket;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.KryoGaWorker;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.LocalGaWorker;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.RestGaWorker;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.WebSocketGaWorker;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.distribution.GaBestResultGetter;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.distribution.GaSimpleMerger;
import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;
import at.ac.uibk.dps.biohadoop.handler.distribution.DistributionConfiguration;
import at.ac.uibk.dps.biohadoop.handler.distribution.DistributionHandler;
import at.ac.uibk.dps.biohadoop.handler.distribution.ZooKeeperConfiguration;
import at.ac.uibk.dps.biohadoop.handler.persistence.file.FileLoadConfiguration;
import at.ac.uibk.dps.biohadoop.handler.persistence.file.FileLoadHandler;
import at.ac.uibk.dps.biohadoop.handler.persistence.file.FileSaveConfiguration;
import at.ac.uibk.dps.biohadoop.handler.persistence.file.FileSaveHandler;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;

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
		CommunicationConfiguration communicationConfiguration = buildCommunicationConfiguration();
		ZooKeeperConfiguration globalDistributionConfiguration = buildGlobalDistributionConfig(local);

		return new BiohadoopConfiguration(version, includePaths, Arrays.asList(
				solverConfig, solverConfig, solverConfig, solverConfig),
				communicationConfiguration, globalDistributionConfiguration);
	}

	private static CommunicationConfiguration buildCommunicationConfiguration() {
		List<Class<? extends MasterLifecycle>> masterEndpoints = new ArrayList<>();
		masterEndpoints.add(GaSocket.class);
		masterEndpoints.add(GaKryo.class);
		masterEndpoints.add(GaRest.class);
		masterEndpoints.add(GaWebSocket.class);
		masterEndpoints.add(GaLocal.class);

		Map<String, Integer> workerEndpoints = new HashMap<>();
		workerEndpoints.put(SocketGaWorker.class.getCanonicalName(), 3);
		workerEndpoints.put(KryoGaWorker.class.getCanonicalName(), 1);
		workerEndpoints.put(RestGaWorker.class.getCanonicalName(), 1);
		workerEndpoints.put(WebSocketGaWorker.class.getCanonicalName(), 1);
		workerEndpoints.put(LocalGaWorker.class.getCanonicalName(), 1);

		return new CommunicationConfiguration(masterEndpoints, workerEndpoints);
	}

	private static SolverConfiguration buildSolverConfig(String name,
			boolean local) {
		AlgorithmConfiguration algorithmConfiguration = buildAlgorithmConfig(local);

		FileSaveConfiguration fileSaveConfiguration = buildFileSaveConfig(local);
		FileLoadConfiguration fileLoadConfiguration = buildFileLoadConfig(local);
		DistributionConfiguration distributionConfiguration = buildDistributionConfig();
		List<HandlerConfiguration> handlers = new ArrayList<>();
		handlers.add(fileSaveConfiguration);
		handlers.add(fileLoadConfiguration);
		handlers.add(distributionConfiguration);

		return new SolverConfiguration(name, algorithmConfiguration, Ga.class,
				handlers);
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

	private static FileSaveConfiguration buildFileSaveConfig(boolean local) {
		String savePath = null;
		if (local) {
			savePath = LOCAL_PERSISTENCE_SAVE_PATH;
		} else {
			savePath = REMOTE_PERSISTENCE_SAVE_PATH;
		}
		return new FileSaveConfiguration(FileSaveHandler.class, savePath, 1000);
	}

	private static FileLoadConfiguration buildFileLoadConfig(boolean local) {
		String loadPath = null;
		if (local) {
			loadPath = LOCAL_PERSISTENCE_LOAD_PATH;
		} else {
			loadPath = REMOTE_PERSISTENCE_LOAD_PATH;
		}
		return new FileLoadConfiguration(FileLoadHandler.class, loadPath, false);
	}

	private static DistributionConfiguration buildDistributionConfig() {
		return new DistributionConfiguration(DistributionHandler.class,
				GaSimpleMerger.class, GaBestResultGetter.class, 2000);
	}

	private static ZooKeeperConfiguration buildGlobalDistributionConfig(
			boolean local) {
		if (local) {
			return new ZooKeeperConfiguration(LOCAL_DISTRIBUTION_INFO_HOST,
					LOCAL_DISTRIBUTION_INFO_PORT);
		} else {
			return new ZooKeeperConfiguration(REMOTE_DISTRIBUTION_INFO_HOST,
					REMOTE_DISTRIBUTION_INFO_PORT);
		}
	}

	private static void readAlgorithmConfig() throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		BiohadoopConfiguration config = null;

		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(new File(LOCAL_OUTPUT_NAME),
				BiohadoopConfiguration.class);
		LOG.info(config.toString());

		config = mapper.readValue(new File(REMOTE_OUTPUT_NAME),
				BiohadoopConfiguration.class);
		LOG.info(config.toString());
	}
}
