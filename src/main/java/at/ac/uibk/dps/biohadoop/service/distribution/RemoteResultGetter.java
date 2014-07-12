package at.ac.uibk.dps.biohadoop.service.distribution;

import java.util.List;

import at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper.NodeData;

public interface RemoteResultGetter {

	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException;
	
}
