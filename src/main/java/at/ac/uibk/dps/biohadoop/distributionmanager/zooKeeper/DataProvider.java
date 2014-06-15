package at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christian Gapp
 *
 */
public class DataProvider implements Watcher {

	private final static Logger LOG = LoggerFactory
			.getLogger(DataProvider.class);

	private final ZooKeeper zooKeeper;
	private final String path;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private volatile List<NodeData> nodesData = new CopyOnWriteArrayList<NodeData>();

	public DataProvider(ZooKeeper zooKeeper, String path) {
		this.zooKeeper = zooKeeper;
		this.path = path;
		updateNodesData();
	}

	@Override
	public void process(WatchedEvent event) {
		LOG.info("Got watch event: {}", event);
		updateNodesData();
	}

	private void updateNodesData() {
		try {
			zooKeeper.getChildren(path, this);
			nodesData = new CopyOnWriteArrayList<>(reloadNodesData());
		} catch (KeeperException | InterruptedException | IOException e) {
			LOG.error("Error while getting ZooKeeper child nodes", e);
		}
	}

	/**
	 * Returns a list of NodeData elements. The list is implemented as
	 * CopyOnWriteArrayList, so it is safe for a consumer to operate on the list
	 * 
	 * @return
	 */
	public List<NodeData> getNodesData() {
		return nodesData;
	}

	private List<NodeData> reloadNodesData() throws KeeperException,
			InterruptedException, IOException {
		final List<String> children = zooKeeper.getChildren(path, this);

		final List<NodeData> newNodesData = new ArrayList<NodeData>();
		for (final String child : children) {
			final byte[] data = zooKeeper.getData(path + "/" + child, false,
					new Stat());
			NodeData nodeData = objectMapper.readValue(data, NodeData.class);
			newNodesData.add(nodeData);
		}
		return newNodesData;
	}

}
