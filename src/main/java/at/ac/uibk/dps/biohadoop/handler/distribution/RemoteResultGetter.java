package at.ac.uibk.dps.biohadoop.handler.distribution;

import java.util.List;

import at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper.NodeData;

public interface RemoteResultGetter {

	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException;
	
}
