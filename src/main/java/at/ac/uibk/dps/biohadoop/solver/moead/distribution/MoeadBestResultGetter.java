package at.ac.uibk.dps.biohadoop.solver.moead.distribution;

import java.util.List;
import java.util.Random;

import at.ac.uibk.dps.biohadoop.service.distribution.DistributionException;
import at.ac.uibk.dps.biohadoop.service.distribution.RemoteDataLoader;
import at.ac.uibk.dps.biohadoop.service.distribution.RemoteResultGetter;
import at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper.NodeData;

public class MoeadBestResultGetter implements RemoteResultGetter {

	private final RemoteDataLoader remoteDataLoader = new RemoteDataLoader();
	private final Random random = new Random();
	
	@Override
	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException {
		if (nodesData == null || nodesData.isEmpty()) {
			return null;
		}
		int index = random.nextInt(nodesData.size());
		return remoteDataLoader.getSolverData(nodesData.get(index));
	}

}
