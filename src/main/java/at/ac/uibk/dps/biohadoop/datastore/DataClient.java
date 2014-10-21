package at.ac.uibk.dps.biohadoop.datastore;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class DataClient {

	public static <T> T getData(SolverId solverId, Option<T> option) {
		return DataService.getInstance().getData(solverId, option);
	}
	
	public static <T> T getData(SolverId solverId, Option<T> option, T defaultValue) {
		T data = getData(solverId, option);
		return data == null ? defaultValue: data;
	}

	public static <T> void setData(SolverId solverId, Option<T> option, T data) {
		DataService.getInstance().setData(solverId, option, data);
	}
	
//	public static SolverData<?> getSolverData(SolverId solverId) {
//		DataService.getInstance().get
//	}
//
//	public static void setDefaultData(SolverId solverId, Object data, double fitness, int iteration) {
//		DataService.getInstance().setData(solverId, DataOptions.DATA, data);
//		DataService.getInstance().setData(solverId, DataOptions.FITNESS, fitness);
//		DataService.getInstance().setData(solverId, DataOptions.ITERATION_STEP, iteration);
//	}
//	
//	public static void setDefaultData(SolverId solverId, Object data, double fitness, int maxIterations, int iteration) {
//		setDefaultData(solverId, data, fitness, iteration);
//		DataService.getInstance().setData(solverId, DataOptions.MAX_ITERATIONS, maxIterations);
//	}

}
