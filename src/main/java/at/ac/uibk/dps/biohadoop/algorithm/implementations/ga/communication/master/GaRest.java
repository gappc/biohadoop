package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.rest.RestResource;

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
