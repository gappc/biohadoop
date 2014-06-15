package at.ac.uibk.dps.biohadoop.distributionmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationHandler;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper.ZooKeeperController;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class DistributionManager implements ApplicationHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(DistributionManager.class);

	private static final DistributionManager DISTRIBUTION_MANAGER = new DistributionManager();
	private Map<ApplicationId, ZooKeeperController> applicationIdToZooKeeper = new ConcurrentHashMap<ApplicationId, ZooKeeperController>();

	public static DistributionManager getInstance() {
		return DistributionManager.DISTRIBUTION_MANAGER;
	}

	// TODO only method accessed by solvers. should maybe be put into separate
	// class
	public <T> ApplicationData<T> getRemoteApplicationData(
			ApplicationId applicationId) throws DistributionException {
		ZooKeeperController zooKeeperManager = applicationIdToZooKeeper
				.get(applicationId);
		return zooKeeperManager.getRemoteApplicationData();
	}

	@Override
	public void onNew(ApplicationId applicationId) {
		try {
			LOG.info("Enabling DistributionManager for Application {}",
					applicationId);
			DistributionConfiguration distributionConfiguration = Environment
					.getBiohadoopConfiguration().getDistributionConfiguration();
			ZooKeeperController zooKeeperController = new ZooKeeperController(
					distributionConfiguration, applicationId);
			applicationIdToZooKeeper.put(applicationId, zooKeeperController);
		} catch (DistributionException e) {
			LOG.error(
					"Error while registering Application {} for DistributionManager",
					applicationId, e);
		}
	}

	@Override
	public void onDataUpdate(ApplicationId applicationId) {
	}

}
