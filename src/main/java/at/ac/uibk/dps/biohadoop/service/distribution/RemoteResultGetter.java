package at.ac.uibk.dps.biohadoop.service.distribution;

import java.util.List;

import at.ac.uibk.dps.biohadoop.service.distribution.zookeeper.NodeData;

public interface RemoteResultGetter {

	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException;
	
}
