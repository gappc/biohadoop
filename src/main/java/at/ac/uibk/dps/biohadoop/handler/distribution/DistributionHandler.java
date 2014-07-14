package at.ac.uibk.dps.biohadoop.handler.distribution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.datastore.DataClient;
import at.ac.uibk.dps.biohadoop.datastore.DataClientImpl;
import at.ac.uibk.dps.biohadoop.datastore.DataOptions;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerBuilder;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.handler.UnknownHandlerException;
import at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper.ZooKeeperController;
import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class DistributionHandler implements Handler {

	private static final Logger LOG = LoggerFactory
			.getLogger(DistributionHandler.class);

	private SolverId solverId;
	private ZooKeeperController zooKeeperController;
	private DistributionConfiguration distributionConfiguration;
	@SuppressWarnings("rawtypes")
	private DataMerger merger;
	private RemoteResultGetter remoteResultGetter;

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		this.solverId = solverId;

		zooKeeperController = getZooKeeperController();
		distributionConfiguration = getDistributionConfiguration();
		merger = getDataMerger();
		remoteResultGetter = getRemoteResultGetter();
	}

	@Override
	public void update(String operation) {
		if (HandlerConstants.DEFAULT.equals(operation)) {
			LOG.debug("{} for application {}", HandlerConstants.ITERATION_STEP,
					solverId);

			DataClient dataClient = new DataClientImpl(solverId);
			int iteration = dataClient.getData(DataOptions.ITERATION_STEP);
			int mergeAfterEveryIteration = distributionConfiguration
					.getMergeAfterIterations();

			if (iteration % mergeAfterEveryIteration == 0) {
				LOG.info("Merging data for solver {}", solverId);
				try {
					Object data = dataClient.getData(DataOptions.DATA);
					List<NodeData> nodeDatas = zooKeeperController
							.getSuitableRemoteNodesData();
					Object remoteData = remoteResultGetter
							.getBestRemoteResult(nodeDatas);

					@SuppressWarnings("unchecked")
					Object mergedData = merger.merge(data, remoteData);

					dataClient.setData(DataOptions.DATA, mergedData);

					LOG.debug("{}: remoteData:        {}", solverId, remoteData);
					LOG.debug("{}: population before: {}", solverId, data);
					LOG.debug("{}: population after:  {}", solverId, mergedData);
				} catch (DataMergeException | DistributionException e) {
					LOG.error("Could not get and merge remote data", e);
				}
			}

		}
	}

	private ZooKeeperController getZooKeeperController()
			throws HandlerInitException {
		try {
			LOG.info("Enabling DistributionService for solver {}", solverId);
			ZooKeeperConfiguration zooKeeperConfiguration = Environment
					.getBiohadoopConfiguration().getZooKeeperConfiguration();
			return new ZooKeeperController(zooKeeperConfiguration, solverId);
		} catch (DistributionException e) {
			LOG.error(
					"Error while registering solver {} for DistributionService",
					solverId, e);
			throw new HandlerInitException(e);
		}
	}

	private DistributionConfiguration getDistributionConfiguration()
			throws HandlerInitException {
		try {
			return HandlerBuilder.getHandlerConfiguration(solverId,
					DistributionConfiguration.class);
		} catch (UnknownHandlerException e) {
			LOG.error(
					"Could not get handler configuration for solver {} and handler {}",
					solverId, DistributionConfiguration.class);
			throw new HandlerInitException(e);
		}
	}

	private DataMerger<?> getDataMerger() throws HandlerInitException {
		try {
			return distributionConfiguration.getDataMerger().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error("Could not instantiate DataMerger {}",
					distributionConfiguration.getDataMerger());
			throw new HandlerInitException(e);
		}
	}

	private RemoteResultGetter getRemoteResultGetter()
			throws HandlerInitException {
		try {
			return distributionConfiguration.getRemoteResultGetter()
					.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error("Could not instantiate RemoteResultGetter {}",
					distributionConfiguration.getDataMerger());
			throw new HandlerInitException(e);
		}
	}
}
