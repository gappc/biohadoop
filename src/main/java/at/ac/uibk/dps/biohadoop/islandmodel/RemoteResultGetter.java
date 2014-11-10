package at.ac.uibk.dps.biohadoop.islandmodel;

import java.util.List;

import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.NodeData;

public interface RemoteResultGetter<T> {

	public T getRemoteData(List<NodeData> nodesData) throws IslandModelException;
	
}
