package at.ac.uibk.dps.biohadoop.islandmodel.zookeeper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.islandmodel.server.IslandModelDataHandler;
import at.ac.uibk.dps.biohadoop.islandmodel.server.IslandModelServer;

public class NodePublisher {

	private static final Logger LOG = LoggerFactory
			.getLogger(NodePublisher.class);

	private final ZooKeeper zooKeeper;
	private final AlgorithmId algorithmId;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NodePublisher(ZooKeeper zooKeeper, AlgorithmId algorithmId) {
		this.zooKeeper = zooKeeper;
		this.algorithmId = algorithmId;
	}

	public AlgorithmWatcher publish(String fullPath) throws KeeperException,
			InterruptedException, IOException {
		String parentPath = getParentPath(fullPath);

		AlgorithmWatcher algorithmWatcher = createParentNodes(parentPath);
		createChildNode(fullPath);

		return algorithmWatcher;
	}

	private AlgorithmWatcher createParentNodes(String parentPath)
			throws KeeperException, InterruptedException {
		String[] parentPathTokens = getPathTokens(parentPath);
		StringBuilder sbPath = new StringBuilder("");
		for (int i = 0; i < parentPathTokens.length; i++) {
			String pathToken = parentPathTokens[i];
			sbPath.append("/").append(pathToken);

			Stat stat = zooKeeper.exists(sbPath.toString(), false);
			if (stat == null) {
				LOG.info("creating " + sbPath);
				try {
					zooKeeper.create(sbPath.toString(), "".getBytes(),
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				} catch (KeeperException.NodeExistsException e) {
					LOG.debug("Node {} already exists", sbPath.toString(), e);
				} catch (InterruptedException e) {
					throw e;
				}
			}
		}

		AlgorithmWatcher dataProvider = new AlgorithmWatcher(zooKeeper,
				parentPath);
		zooKeeper.getChildren(parentPath, dataProvider);

		return dataProvider;
	}

	private void createChildNode(String fullPath) throws IOException,
			KeeperException, InterruptedException {
		NodeData nodeData = getNodeData();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		objectMapper.writeValue(bos, nodeData);

		zooKeeper.create(fullPath, bos.toByteArray(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL);
	}

	private String getParentPath(String path) {
		int lastPathDelimiterIndex = path.lastIndexOf("/");
		if (lastPathDelimiterIndex > 0) {
			return path.substring(0, lastPathDelimiterIndex);
		} else {
			throw new IllegalArgumentException("Given path \"" + path
					+ "\" has no parent element");
		}
	}

	private String[] getPathTokens(String path) {
		String cleanPath = null;
		if (path.charAt(0) == '/') {
			cleanPath = path.substring(1);
		}
		if (path.charAt(path.length() - 1) == '/') {
			cleanPath = path.substring(0, path.length() - 1);
		}

		return cleanPath.split("/");
	}

	private NodeData getNodeData() {
		String host = Environment.get(IslandModelServer.ISLAND_MODEL_HOST);
		String port = Environment.get(IslandModelServer.ISLAND_MODEL_PORT);

		// TODO hardcoding http as protocol is a bad idea
		StringBuilder islandModelResourceUrl = new StringBuilder();
		islandModelResourceUrl.append("http://").append(host).append(":")
				.append(port).append("/" + IslandModelDataHandler.PATH);

		return new NodeData(algorithmId, islandModelResourceUrl.toString());
	}

}
