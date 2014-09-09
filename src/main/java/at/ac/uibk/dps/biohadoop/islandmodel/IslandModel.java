package at.ac.uibk.dps.biohadoop.islandmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.ZooKeeperController;
import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class IslandModel {

	public static final String ISLAND_MERGE_AFTER_ITERATION = "ISLAND_MERGE_AFTER_ITERATION";
	public static final String ISLAND_DATA_MERGER = "ISLAND_DATA_MERGER";
	public static final String ISLAND_DATA_REMOTE_RESULT_GETTER = "ISLAND_DATA_REMOTE_RESULT_GETTER";

	private static final Logger LOG = LoggerFactory
			.getLogger(IslandModel.class);

	private static final Map<SolverId, ZooKeeperController> zooKeeperControllers = new HashMap<>();

	public static void initialize(SolverId solverId)
			throws IslandModelException {
		getZooKeeperController(solverId);
	}

	public static Object merge(SolverId solverId,
			Map<String, String> properties, SolverData<?> solverData)
			throws IslandModelException {
		ZooKeeperController zooKeeperController = getZooKeeperController(solverId);
		RemoteResultGetter remoteResultGetter = getRemoteResultGetter(properties);
		DataMerger<Object> dataMerger = (DataMerger<Object>) getDataMerger(properties);
		LOG.info("Merging data for solver {}", solverId);
		List<NodeData> nodeDatas = zooKeeperController
				.getSuitableRemoteNodesData();
		Object remoteData = remoteResultGetter.getBestRemoteResult(nodeDatas);

		Object mergedData;
		mergedData = dataMerger.merge(solverData.getData(), remoteData);

		LOG.debug("{}: remoteData:        {}", solverId, remoteData);
		LOG.debug("{}: population before: {}", solverId, solverData.getData());
		LOG.debug("{}: population after:  {}", solverId, mergedData);

		return mergedData;
	}

	private static ZooKeeperController getZooKeeperController(SolverId solverId)
			throws IslandModelException {
		String hostname = Environment.getBiohadoopConfiguration()
				.getGlobalProperties()
				.get(ZooKeeperController.ZOOKEEPER_HOSTNAME);
		if (hostname == null) {
			throw new IslandModelException(
					"Could not read ZooKeeper hostname from global properties");
		}

		String port = Environment.getBiohadoopConfiguration()
				.getGlobalProperties().get(ZooKeeperController.ZOOKEEPER_PORT);
		if (port == null) {
			throw new IslandModelException(
					"Could not read ZooKeeper port from global properties");
		}

		ZooKeeperController zooKeeperController = null;
		synchronized (zooKeeperControllers) {
			zooKeeperController = zooKeeperControllers.get(solverId);
			if (zooKeeperController == null) {
				zooKeeperController = new ZooKeeperController(solverId,
						hostname, port);
				zooKeeperControllers.put(solverId, zooKeeperController);
			}
		}

		return zooKeeperController;
	}

	private static DataMerger<?> getDataMerger(Map<String, String> properties)
			throws IslandModelException {
		try {
			String dataMergerClass = properties.get(ISLAND_DATA_MERGER);
			if (dataMergerClass == null) {
				throw new IslandModelException(
						"DataMerger for Island model is null, property "
								+ ISLAND_DATA_MERGER + " not defined");
			}
			Class<? extends DataMerger<?>> dataMerger = (Class<? extends DataMerger<?>>) Class
					.forName(dataMergerClass);
			return dataMerger.newInstance();
		} catch (InstantiationException | ClassNotFoundException
				| IllegalAccessException e) {
			throw new IslandModelException("Could not instantiate DataMerger",
					e);
		}
	}

	private static RemoteResultGetter getRemoteResultGetter(
			Map<String, String> properties) throws IslandModelException {
		try {
			String remoteResultGetterClass = properties
					.get(ISLAND_DATA_REMOTE_RESULT_GETTER);
			if (remoteResultGetterClass == null) {
				throw new IslandModelException(
						"RemoteResultGetter for Island model is null, property "
								+ ISLAND_DATA_REMOTE_RESULT_GETTER
								+ " not defined");
			}
			Class<? extends RemoteResultGetter> remoteResultGetter = (Class<? extends RemoteResultGetter>) Class
					.forName(remoteResultGetterClass);
			return remoteResultGetter.newInstance();
		} catch (InstantiationException | ClassNotFoundException
				| IllegalAccessException e) {
			throw new IslandModelException(
					"Could not instantiate RemoteResultGetter", e);
		}
	}
}
