package at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.service.distribution.DistributionException;
import at.ac.uibk.dps.biohadoop.service.distribution.GlobalDistributionConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;

public class ZooKeeperController {

	private final static Logger LOG = LoggerFactory
			.getLogger(ZooKeeperController.class);

	private final static String SOLVER_PATH = "/biohadoop/solvers";
	private final ZooKeeper zooKeeper;
	private final RegistrationProvider registrationProvider;
	private final SolverId solverId;

	// TODO check if other design is better suited (because of large
	// constructor)
	public ZooKeeperController(
			GlobalDistributionConfiguration globalDistributionConfiguration,
			SolverId solverId) throws DistributionException {

		this.solverId = solverId;
		final CountDownLatch latch = new CountDownLatch(1);
		final String url = globalDistributionConfiguration.getHost() + ":"
				+ globalDistributionConfiguration.getPort();

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
			throw new DistributionException(
					"Error while connecting to ZooKeeper at " + url, e);
		}

		try {
			String fullPath = getFullPath();

			registrationProvider = new RegistrationProvider(zooKeeper);
			registrationProvider.registerNode(fullPath, solverId);
		} catch (IOException | KeeperException | InterruptedException e) {
			throw new DistributionException(
					"Error while registering to ZooKeeper at " + url, e);
		}
	}

	public SolverData<?> getRemoteSolverData()
			throws DistributionException {
		List<NodeData> remoteNodesData = getSuitableRemoteNodesData();

		int index = ThreadLocalRandom.current().nextInt(remoteNodesData.size());
		NodeData nodeData = remoteNodesData.get(index);
		LOG.info("Solver {} gets remote data from Solver {}", solverId,
				nodeData.getSolverId());
		String path = nodeData.getUrl() + "/" + nodeData.getSolverId();

		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(path)
					.request(MediaType.APPLICATION_JSON).get();
			return response.readEntity(SolverData.class);
		} catch(Exception e) {
			throw new DistributionException("Could not connect to " + path, e);
		}
	}

	private String getFullPath() {
		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		Class<? extends Algorithm<?, ?>> algorithmType = solverConfiguration
				.getAlgorithm();

		StringBuilder sb = new StringBuilder().append(SOLVER_PATH)
				.append("/").append(algorithmType.getSimpleName()).append("/")
				.append(solverId);
		return sb.toString();
	}

	private List<NodeData> getSuitableRemoteNodesData() throws DistributionException {
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