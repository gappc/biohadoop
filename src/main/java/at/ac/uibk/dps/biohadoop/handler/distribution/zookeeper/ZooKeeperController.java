package at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;
import at.ac.uibk.dps.biohadoop.handler.distribution.IslandModelException;
import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.solver.SolverService;

public class ZooKeeperController {
	
	public static final String ZOOKEEPER_HOSTNAME = "ZOOKEEPER_HOSTNAME";
	public static final String ZOOKEEPER_PORT = "ZOOKEEPER_PORT";

	private static final Logger LOG = LoggerFactory
			.getLogger(ZooKeeperController.class);

	private static final String SOLVER_PATH = "/biohadoop/solvers";
	private final ZooKeeper zooKeeper;
	private final RegistrationProvider registrationProvider;
	private final SolverId solverId;

	// TODO check if other design is better suited (because of large
	// constructor)
	public ZooKeeperController(SolverId solverId, String hostname, String port)
			throws IslandModelException {

		this.solverId = solverId;
		final CountDownLatch latch = new CountDownLatch(1);
		final String url = hostname + ":" + port;

		// Connect to ZooKeeper
		try {
			zooKeeper = new ZooKeeper(url, 2000, new Watcher() {
				@Override
				public void process(WatchedEvent arg0) {
					LOG.info("Connection to ZooKeeper successful");
					latch.countDown();
				}
			});

			latch.await();
		} catch (InterruptedException | IOException e) {
			throw new IslandModelException(
					"Error while connecting to ZooKeeper at " + url, e);
		}

		try {
			String fullPath = getFullPath();

			registrationProvider = new RegistrationProvider(zooKeeper);
			registrationProvider.registerNode(fullPath, solverId);
		} catch (IOException | KeeperException | InterruptedException e) {
			throw new IslandModelException(
					"Error while registering to ZooKeeper at " + url, e);
		}
	}

	private String getFullPath() {
		SolverService solverService = SolverService.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		Class<? extends Algorithm> algorithmType = solverConfiguration
				.getAlgorithm();

		StringBuilder sb = new StringBuilder().append(SOLVER_PATH).append("/")
				.append(algorithmType.getSimpleName()).append("/")
				.append(solverId);
		return sb.toString();
	}

	public List<NodeData> getSuitableRemoteNodesData() {
		List<NodeData> remoteNodesData = registrationProvider.getNodesData();
		// remove self from list
		NodeData self = null;
		for (NodeData nodeData : remoteNodesData) {
			if (solverId.equals(nodeData.getSolverId())) {
				LOG.debug("marking self solver {} for removal", solverId);
				self = nodeData;
			}
		}
		if (self != null) {
			remoteNodesData.remove(self);
		}
		return remoteNodesData;
	}

}
