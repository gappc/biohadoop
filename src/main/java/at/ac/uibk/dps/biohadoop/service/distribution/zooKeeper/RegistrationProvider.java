package at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class RegistrationProvider {
	
	private final ZooKeeper zooKeeper;
	private DataProvider dataProvider;
	
	public RegistrationProvider(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	public void registerNode(String path, SolverId solverId) throws KeeperException, InterruptedException, IOException {
		NodePublisher nodePublisher = new NodePublisher(zooKeeper, solverId);
		dataProvider = nodePublisher.publish(path);
	}

	public List<NodeData> getNodesData() {
		return dataProvider.getNodesData();
	}
}
