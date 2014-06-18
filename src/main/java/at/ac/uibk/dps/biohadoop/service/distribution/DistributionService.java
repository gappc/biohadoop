package at.ac.uibk.dps.biohadoop.service.distribution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper.ZooKeeperController;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverHandler;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;

public class DistributionService implements SolverHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(DistributionService.class);

	private static final DistributionService DISTRIBUTION_SERVICE = new DistributionService();
	private Map<SolverId, ZooKeeperController> solverIdToZooKeeper = new ConcurrentHashMap<SolverId, ZooKeeperController>();

	public static DistributionService getInstance() {
		return DistributionService.DISTRIBUTION_SERVICE;
	}

	@Override
	public void onNew(SolverId solverId) {
		try {
			LOG.info("Enabling DistributionService for solver {}",
					solverId);
			GlobalDistributionConfiguration globalDistributionConfiguration = Environment
					.getBiohadoopConfiguration()
					.getGlobalDistributionConfiguration();
			ZooKeeperController zooKeeperController = new ZooKeeperController(
					globalDistributionConfiguration, solverId);
			solverIdToZooKeeper.put(solverId, zooKeeperController);
		} catch (DistributionException e) {
			LOG.error(
					"Error while registering solver {} for DistributionService",
					solverId, e);
		}
	}

	@Override
	public void onDataUpdate(SolverId solverId) {
		LOG.debug("onDataUpdate for applcation {}", solverId);

		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		SolverData<?> solverData = solverService
				.getSolverData(solverId);

		int mergeAfterEveryIteration = 1000;

		if (solverData.getIteration() % mergeAfterEveryIteration == 0) {
			LOG.info(
					"Merging data for solver with name {} and solverId {}",
					solverConfiguration.getName(), solverId);
			Class<? extends DataMerger> mergerClass = solverConfiguration
					.getDistributionConfiguration().getDataMerger();
			try {
				SolverData<?> remoteSolverData = getRemoteSolverData(solverId);

				DataMerger merger = mergerClass.newInstance();
				Object mergedData = merger.merge(solverData.getData(),
						remoteSolverData.getData());
				SolverData<?> mergedSolverData = new SolverData<Object>(
						mergedData, -1, solverService.getSolverData(
								solverId).getIteration());
				solverService.updateSolverData(solverId,
						mergedSolverData);

				LOG.info("{}: remoteData:        {}", Thread.currentThread(),
						remoteSolverData.getData());
				LOG.info("{}: population before: {}", Thread.currentThread(),
						solverData.getData());
				LOG.info("{}: population after:  {}", Thread.currentThread(),
						mergedSolverData.getData());
			} catch (DataMergeException | DistributionException e) {
				LOG.error("Could not get and merge remote data");
			} catch (InstantiationException | IllegalAccessException e) {
				LOG.error("Could not instanciate merger from class {}", mergerClass.getCanonicalName(), e);
			}
		}
	}

	private SolverData<?> getRemoteSolverData(
			SolverId solverId) throws DistributionException {
		ZooKeeperController zooKeeperManager = solverIdToZooKeeper
				.get(solverId);
		return zooKeeperManager.getRemoteSolverData();
	}
}
