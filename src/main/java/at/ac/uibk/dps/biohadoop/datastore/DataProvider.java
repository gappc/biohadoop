package at.ac.uibk.dps.biohadoop.datastore;

import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class DataProvider {

	private DataProvider() {
	}

	public static SolverData<?> getSolverData(SolverId solverId) {
		DataClient dataClient = new DataClientImpl(solverId);
		Object data = dataClient.getData(DataOptions.DATA);
		Double fitness = dataClient.getData(DataOptions.FITNESS);

		Integer iterationStart = dataClient
				.getData(DataOptions.ITERATION_START);
		iterationStart = iterationStart == null ? 0 : iterationStart;
		Integer iteration = dataClient.getData(DataOptions.ITERATION_STEP)
				+ iterationStart;

		return new SolverData<Object>(data, fitness, iteration);
	}
}
