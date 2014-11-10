package at.ac.uibk.dps.biohadoop.islandmodel.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmService;
import at.ac.uibk.dps.biohadoop.islandmodel.IslandModelException;

public class ZooKeeperController {

	public static final String ZOOKEEPER_HOSTNAME = "ZOOKEEPER_HOSTNAME";
	public static final String ZOOKEEPER_PORT = "ZOOKEEPER_PORT";

	private static final Logger LOG = LoggerFactory
			.getLogger(ZooKeeperController.class);

	private static final String ALGORITHM_PATH = "/biohadoop/algorithms";
	private static final int TIMEOUT_SEC = 5;

	private final RegistrationProvider registrationProvider;
	private final AlgorithmId algorithmId;

	public ZooKeeperController(AlgorithmId algorithmId, String hostname, String port)
			throws IslandModelException {

		this.algorithmId = algorithmId;
		final String url = hostname + ":" + port;

		// Connect to ZooKeeper
		ZooKeeper zooKeeper = null;
		ZooKeeperConnector zkConnector = null;
		try {
			zkConnector = new ZooKeeperConnector();
			zooKeeper = zkConnector.initialize(url);

			ExecutorService executor = Executors.newFixedThreadPool(1);
			Future<Object> zkFuture = executor.submit(zkConnector);
			executor.shutdown();
			zkFuture.get(TIMEOUT_SEC, TimeUnit.SECONDS);
		} catch (IOException | InterruptedException | ExecutionException
				| TimeoutException e) {
			String errMsg = null;
			if (e.getClass() == TimeoutException.class) {
				errMsg = "Timeout after " + TIMEOUT_SEC
						+ " seconds while trying to connect to ZooKeeper at "
						+ url;
			} else {
				errMsg = "Error while connecting to ZooKeeper at " + url;
			}
			zkConnector.unblock();
			throw new IslandModelException(errMsg, e);
		}

		try {
			String fullPath = getFullPath();

			registrationProvider = new RegistrationProvider(zooKeeper);
			registrationProvider.registerNode(fullPath, algorithmId);
		} catch (IOException | KeeperException | InterruptedException e) {
			throw new IslandModelException(
					"Error while registering to ZooKeeper at " + url, e);
		}
	}

	private String getFullPath() {
		AlgorithmConfiguration algorithmConfiguration = AlgorithmService
				.getAlgorithmConfiguration(algorithmId);
		Class<? extends Algorithm> algorithmType = algorithmConfiguration
				.getAlgorithm();

		StringBuilder sb = new StringBuilder().append(ALGORITHM_PATH).append("/")
				.append(algorithmType.getSimpleName()).append("/")
				.append(algorithmId);
		return sb.toString();
	}

	public List<NodeData> getSuitableRemoteNodesData() {
		List<NodeData> remoteNodesData = registrationProvider.getNodesData();
		// remove self from list
		NodeData self = null;
		for (NodeData nodeData : remoteNodesData) {
			if (algorithmId.equals(nodeData.getAlgorithmId())) {
				LOG.debug("marking self algorithm {} for removal", algorithmId);
				self = nodeData;
			}
		}
		if (self != null) {
			remoteNodesData.remove(self);
		}
		return remoteNodesData;
	}

}
