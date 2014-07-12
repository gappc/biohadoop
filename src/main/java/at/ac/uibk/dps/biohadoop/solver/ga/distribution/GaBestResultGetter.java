package at.ac.uibk.dps.biohadoop.solver.ga.distribution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.ac.uibk.dps.biohadoop.service.distribution.DistributionException;
import at.ac.uibk.dps.biohadoop.service.distribution.RemoteDataLoader;
import at.ac.uibk.dps.biohadoop.service.distribution.RemoteResultGetter;
import at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper.NodeData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;

public class GaBestResultGetter implements RemoteResultGetter {

	private final RemoteDataLoader remoteDataLoader = new RemoteDataLoader();
	
	@Override
	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException {
		if (nodesData == null || nodesData.size() == 0) {
			return null;
		}
		List<SolverData<?>> solverDatas = remoteDataLoader.getSolverDatas(nodesData);
		Collections.sort(solverDatas, new Comparator<SolverData<?>>() {
			@Override
			public int compare(SolverData<?> o1, SolverData<?> o2) {
				return (int)Math.signum(o1.getFitness() - o2.getFitness());
			}
		});
		return solverDatas.get(0).getData();
	}

}
