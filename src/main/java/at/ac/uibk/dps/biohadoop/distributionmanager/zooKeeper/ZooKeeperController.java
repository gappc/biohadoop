package at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper;

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

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.distributionmanager.GlobalDistributionConfiguration;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionException;

public class ZooKeeperController {

	private final static Logger LOG = LoggerFactory
			.getLogger(ZooKeeperController.class);

	private final static String APPLICATION_PATH = "/biohadoop/applications";
	private final ZooKeeper zooKeeper;
	private final RegistrationProvider registrationProvider;
	private final ApplicationId applicationId;

	// TODO check if other design is better suited (because of large
	// constructor)
	public ZooKeeperController(
			GlobalDistributionConfiguration globalDistributionConfiguration,
			ApplicationId applicationId) throws DistributionException {

		this.applicationId = applicationId;
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
			registrationProvider.registerNode(fullPath, applicationId);
		} catch (IOException | KeeperException | InterruptedException e) {
			throw new DistributionException(
					"Error while registering to ZooKeeper at " + url, e);
		}
	}

	public ApplicationData<?> getRemoteApplicationData()
			throws DistributionException {
		List<NodeData> remoteNodesData = getSuitableRemoteNodesData();

		int index = ThreadLocalRandom.current().nextInt(remoteNodesData.size());
		NodeData nodeData = remoteNodesData.get(index);
		LOG.info("Solver {} gets remote data from Solver {}", applicationId,
				nodeData.getApplicationId());
		String path = nodeData.getUrl() + "/" + nodeData.getApplicationId();

		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(path)
					.request(MediaType.APPLICATION_JSON).get();
			return response.readEntity(ApplicationData.class);
		} catch(Exception e) {
			throw new DistributionException("Could not connect to " + path, e);
		}
	}

	private String getFullPath() {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		Class<? extends Algorithm<?, ?>> algorithmType = applicationConfiguration
				.getAlgorithm();

		StringBuilder sb = new StringBuilder().append(APPLICATION_PATH)
				.append("/").append(algorithmType.getSimpleName()).append("/")
				.append(applicationId);
		return sb.toString();
	}

	private List<NodeData> getSuitableRemoteNodesData() throws DistributionException {
		List<NodeData> remoteNodesData = registrationProvider.getNodesData();
		// remove self from list
		NodeData self = null;
		for (NodeData nodeData : remoteNodesData) {
			if (applicationId.equals(nodeData.getApplicationId())) {
				LOG.debug("marking self aplicatoin {} for removal", applicationId);
				self = nodeData;
			}
		}
		if (self != null) {
			remoteNodesData.remove(self);
		}
		return remoteNodesData;
	}

}
