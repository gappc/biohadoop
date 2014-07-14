package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.master;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.rest.RestResource;

@Path("/nsgaii")
@Produces(MediaType.APPLICATION_JSON)
public class NsgaIIRest extends RestResource<double[], double[]> {

	@Override
	public String getQueueName() {
		return NsgaII.NSGAII_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

	@Override
	public TypeReference<Message<double[]>> getInputType() {
		return new TypeReference<Message<double[]>>() {
		};
	}

}
