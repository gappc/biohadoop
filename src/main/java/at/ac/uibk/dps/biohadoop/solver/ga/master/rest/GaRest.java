package at.ac.uibk.dps.biohadoop.solver.ga.master.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.rest.RestResource;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

import com.fasterxml.jackson.core.type.TypeReference;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRest extends RestResource<int[], Double> {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}

	@Override
	public TypeReference<Message<Double>> getInputType() {
		return new TypeReference<Message<Double>>() {
		};
	}

}
