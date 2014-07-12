package at.ac.uibk.dps.biohadoop.datastore;

import org.xnio.Option;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class DataClientImpl implements DataClient {

	private final SolverId solverId;
	
	public DataClientImpl(SolverId solverId) {
		this.solverId = solverId;
	}
	
	@Override
	public <T> T getData(Option<T> option) {
		return DataService.getInstance().getData(solverId, option);
	}
	
	@Override
	public <T> T getData(Option<T> option, T defaultValue) {
		T data = getData(option);
		return data == null ? defaultValue: data;
	}

	@Override
	public <T> void setData(Option<T> option, T data) {
		DataService.getInstance().setData(solverId, option, data);
	}

	@Override
	public void setDefaultData(Object data, double fitness, int iteration) {
		DataService.getInstance().setData(solverId, DataOptions.DATA, data);
		DataService.getInstance().setData(solverId, DataOptions.FITNESS, fitness);
		DataService.getInstance().setData(solverId, DataOptions.ITERATION_STEP, iteration);
	}
	
	@Override
	public void setDefaultData(Object data, double fitness, int maxIterations, int iteration) {
		setDefaultData(data, fitness, iteration);
		DataService.getInstance().setData(solverId, DataOptions.MAX_ITERATIONS, maxIterations);
	}

}
