package at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NodePublisher {

	private final static Logger LOG = LoggerFactory
			.getLogger(NodePublisher.class);

	private final ZooKeeper zooKeeper;
	private final ApplicationId applicationId;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NodePublisher(ZooKeeper zooKeeper, ApplicationId applicationId) {
		this.zooKeeper = zooKeeper;
		this.applicationId = applicationId;
	}

	public DataProvider publish(String fullPath) throws KeeperException, InterruptedException, IOException {
		String parentPath = getParentPath(fullPath);
		
		DataProvider childrenWatcher = createParentNodes(parentPath);
		createChildNode(fullPath);
		
		return childrenWatcher;
	}
	
	private DataProvider createParentNodes(String parentPath) throws KeeperException, InterruptedException {
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
				} catch (KeeperException | InterruptedException e) {
					if (e instanceof KeeperException.NodeExistsException) {
						LOG.info("Node {} already exists", sbPath.toString());
					}
					else {
						throw e;
					}
				}
			}
		}
		
		DataProvider dataProvider = new DataProvider(zooKeeper, parentPath);
		zooKeeper.getChildren(parentPath, dataProvider);
		
		return dataProvider;
	}

	private void createChildNode(String fullPath) throws IOException, KeeperException,
			InterruptedException {
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
		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		if (path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		
		return path.split("/");
	}

	private NodeData getNodeData() {
		String host = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);

		// TODO hardcoding http as protocol is a bad idea
		StringBuilder distributionResourceUrl = new StringBuilder();
		distributionResourceUrl.append("http://").append(host).append(":")
				.append(port).append("/rs/distribution");

		NodeData nodeData = new NodeData(applicationId,
				distributionResourceUrl.toString());
		return nodeData;
	}

}
