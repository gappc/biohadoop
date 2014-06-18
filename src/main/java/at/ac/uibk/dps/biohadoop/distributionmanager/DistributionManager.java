package at.ac.uibk.dps.biohadoop.distributionmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationHandler;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
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

	@Override
	public void onNew(ApplicationId applicationId) {
		try {
			LOG.info("Enabling DistributionManager for Application {}",
					applicationId);
			GlobalDistributionConfiguration globalDistributionConfiguration = Environment
					.getBiohadoopConfiguration()
					.getGlobalDistributionConfiguration();
			ZooKeeperController zooKeeperController = new ZooKeeperController(
					globalDistributionConfiguration, applicationId);
			applicationIdToZooKeeper.put(applicationId, zooKeeperController);
		} catch (DistributionException e) {
			LOG.error(
					"Error while registering Application {} for DistributionManager",
					applicationId, e);
		}
	}

	@Override
	public void onDataUpdate(ApplicationId applicationId) {
		LOG.debug("onDataUpdate for applcation {}", applicationId);

		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		ApplicationData<?> applicationData = applicationManager
				.getApplicationData(applicationId);

		int mergeAfterEveryIteration = 1000;

		if (applicationData.getIteration() % mergeAfterEveryIteration == 0) {
			LOG.info(
					"Merging data for application with name {} and applicationId {}",
					applicationConfiguration.getName(), applicationId);
			Class<? extends DataMerger> mergerClass = applicationConfiguration
					.getDistributionConfiguration().getDataMerger();
			try {
				ApplicationData<?> remoteApplicationData = getRemoteApplicationData(applicationId);

				DataMerger merger = mergerClass.newInstance();
				Object mergedData = merger.merge(applicationData.getData(),
						remoteApplicationData.getData());
				ApplicationData<?> mergedApplicationData = new ApplicationData<Object>(
						mergedData, -1, applicationManager.getApplicationData(
								applicationId).getIteration());
				applicationManager.updateApplicationData(applicationId,
						mergedApplicationData);

				LOG.info("{}: remoteData:        {}", Thread.currentThread(),
						remoteApplicationData.getData());
				LOG.info("{}: population before: {}", Thread.currentThread(),
						applicationData.getData());
				LOG.info("{}: population after:  {}", Thread.currentThread(),
						mergedApplicationData.getData());
			} catch (DataMergeException | DistributionException e) {
				LOG.error("Could not get and merge remote data");
			} catch (InstantiationException | IllegalAccessException e) {
				LOG.error("Could not instanciate merger from class {}", mergerClass.getCanonicalName(), e);
			}
		}
	}

	private ApplicationData<?> getRemoteApplicationData(
			ApplicationId applicationId) throws DistributionException {
		ZooKeeperController zooKeeperManager = applicationIdToZooKeeper
				.get(applicationId);
		return zooKeeperManager.getRemoteApplicationData();
	}
}
