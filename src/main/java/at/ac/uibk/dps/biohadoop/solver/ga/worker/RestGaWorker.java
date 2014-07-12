package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connectionworker.RestWorker;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.rest.GaRest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestGaWorker extends RestWorker<int[], Double> {

	private double[][] distances;

	@Override
	public Class<? extends Master> getMasterEndpoint() {
		return GaRest.class;
	}
	
	@Override
	public void readRegistrationObject(Object data) {
		ObjectMapper mapper = new ObjectMapper();
		distances = mapper.convertValue(data, double[][].class);
	}

	@Override
	public Double compute(int[] data) {
		return GaFitness.computeFitness(distances, data);
	}

	@Override
	public String getPath() {
		return "/ga";
	}

	@Override
	public TypeReference<Message<int[]>> getInputType() {
		return new TypeReference<Message<int[]>>() {
		};
	}

}
