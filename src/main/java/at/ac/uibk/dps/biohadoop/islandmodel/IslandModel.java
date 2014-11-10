package at.ac.uibk.dps.biohadoop.islandmodel;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.ZooKeeperController;
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
		checkZooKeeper();
		getZooKeeperController(solverId);
	}

	private static void checkZooKeeper() throws IslandModelException {
		String hostname = getZooKeeperHostname();
		String port = getZooKeeperPort();
		Socket socket = null;
		try {
			socket = new Socket(hostname, Integer.parseInt(port));
			return;
		} catch (Exception e) {
			throw new IslandModelException("Could not connect to ZooKeeper", e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					throw new IslandModelException("Error while closing socket connection to ZooKeeper", e);
				}
			}
		}
	}

	public static void publish(SolverId solverId, Object data) {
		IslandModelResource.publish(solverId, data);
	}
	
	public static <T>T merge(SolverId solverId,
			Map<String, String> properties, T data)
			throws IslandModelException {
		ZooKeeperController zooKeeperController = getZooKeeperController(solverId);
		RemoteResultGetter<T> remoteResultGetter = getRemoteResultGetter(properties);
		DataMerger<T> dataMerger = getDataMerger(properties);
		List<NodeData> nodeDatas = zooKeeperController
				.getSuitableRemoteNodesData();
		
		LOG.info("Merging data for solver {}", solverId);
		
		T remoteData = remoteResultGetter.getRemoteData(nodeDatas);
		T mergedData = dataMerger.merge(data, remoteData);

		LOG.debug("{}: remoteData:        {}", solverId, remoteData);
		LOG.debug("{}: population before: {}", solverId, data);
		LOG.debug("{}: population after:  {}", solverId, mergedData);

		return mergedData;
	}

	private static ZooKeeperController getZooKeeperController(SolverId solverId)
			throws IslandModelException {
		String hostname = getZooKeeperHostname();
		String port = getZooKeeperPort();
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

	private static <T>DataMerger<T> getDataMerger(Map<String, String> properties)
			throws IslandModelException {
		try {
			String dataMergerClass = properties.get(ISLAND_DATA_MERGER);
			if (dataMergerClass == null) {
				throw new IslandModelException(
						"DataMerger for Island model is null, property "
								+ ISLAND_DATA_MERGER + " not defined");
			}
			@SuppressWarnings("unchecked")
			Class<? extends DataMerger<T>> dataMerger = (Class<? extends DataMerger<T>>) Class
					.forName(dataMergerClass);
			return dataMerger.newInstance();
		} catch (InstantiationException | ClassNotFoundException
				| IllegalAccessException e) {
			throw new IslandModelException("Could not instantiate DataMerger",
					e);
		}
	}

	private static <T>RemoteResultGetter<T> getRemoteResultGetter(
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
			@SuppressWarnings("unchecked")
			Class<? extends RemoteResultGetter<T>> remoteResultGetter = (Class<? extends RemoteResultGetter<T>>) Class
					.forName(remoteResultGetterClass);
			return remoteResultGetter.newInstance();
		} catch (InstantiationException | ClassNotFoundException
				| IllegalAccessException e) {
			throw new IslandModelException(
					"Could not instantiate RemoteResultGetter", e);
		}
	}

	private static String getZooKeeperHostname() throws IslandModelException {
		String hostname = Environment.getBiohadoopConfiguration()
				.getGlobalProperties()
				.get(ZooKeeperController.ZOOKEEPER_HOSTNAME);
		if (hostname == null) {
			throw new IslandModelException(
					"Could not read ZooKeeper hostname from global properties");
		}
		return hostname;
	}

	private static String getZooKeeperPort() throws IslandModelException {
		String port = Environment.getBiohadoopConfiguration()
				.getGlobalProperties().get(ZooKeeperController.ZOOKEEPER_PORT);
		if (port == null) {
			throw new IslandModelException(
					"Could not read ZooKeeper port from global properties");
		}
		return port;
	}
}
