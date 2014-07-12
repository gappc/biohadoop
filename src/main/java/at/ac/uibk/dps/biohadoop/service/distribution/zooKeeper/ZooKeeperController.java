package at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.service.distribution.DistributionException;
import at.ac.uibk.dps.biohadoop.service.distribution.ZooKeeperConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
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
			ZooKeeperConfiguration globalDistributionConfiguration,
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
	
//	public SolverData<?> getRemoteSolverData() throws DistributionException {
//		List<NodeData> remoteNodesData = getSuitableRemoteNodesData();
//		int remoteNodesCount = remoteNodesData.size();
//
//		if (remoteNodesCount == 0) {
//			LOG.error("No suitable node found");
//			throw new DistributionException("No suitable node found");
//		}
//		
//		int index = ThreadLocalRandom.current().nextInt(remoteNodesData.size());
//		NodeData nodeData = remoteNodesData.get(index);
//		LOG.info("Solver {} gets remote data from Solver {}", solverId,
//				nodeData.getSolverId());
//		String path = nodeData.getUrl() + "/" + nodeData.getSolverId() + "/typed";
//
//		Response response = null;
//		try {
//			Client client = ClientBuilder.newClient();
//			response = client.target(path)
//					.request(MediaType.APPLICATION_JSON).get();
//		} catch (Exception e) {
//			LOG.error("Could not get remote data from {}", path, e);
//			throw new DistributionException(e);
//		}
//		
//		try {
//			String dataString = response.readEntity(String.class);
//			SolverData<?> solverData = objectMapper.readValue(dataString, SolverData.class);
//			if (solverData == null) {
//				LOG.error("No remote data found at {}", path);
//				throw new DistributionException("No remote data found");
//			}
//			return solverData;
//		} catch (Exception e) {
//			LOG.error("Error while deserialization of resource {}", path, e);
//			throw new DistributionException(e);
//		}
//	}

	private String getFullPath() {
		SolverService solverService = SolverService.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		Class<? extends Algorithm<?, ?>> algorithmType = solverConfiguration
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
