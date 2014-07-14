package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.distribution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.ac.uibk.dps.biohadoop.handler.distribution.DistributionException;
import at.ac.uibk.dps.biohadoop.handler.distribution.RemoteDataLoader;
import at.ac.uibk.dps.biohadoop.handler.distribution.RemoteResultGetter;
import at.ac.uibk.dps.biohadoop.handler.distribution.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.solver.SolverData;

public class GaBestResultGetter implements RemoteResultGetter {

	private final RemoteDataLoader remoteDataLoader = new RemoteDataLoader();
	
	@Override
	public Object getBestRemoteResult(List<NodeData> nodesData) throws DistributionException {
		if (nodesData == null || nodesData.isEmpty()) {
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
