package at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;

public class RegistrationProvider {
	
	private final ZooKeeper zooKeeper;
	private DataProvider dataProvider;
	
	public RegistrationProvider(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	public void registerNode(String path, ApplicationId applicationId) throws KeeperException, InterruptedException, IOException {
		NodePublisher nodePublisher = new NodePublisher(zooKeeper, applicationId);
		dataProvider = nodePublisher.publish(path);
	}

	public List<NodeData> getNodesData() {
		return dataProvider.getNodesData();
	}
}
