package at.ac.uibk.dps.biohadoop.solver.nsgaii.master;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.connection.rest.RestResource;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;

@Path("/nsgaii")
@Produces(MediaType.APPLICATION_JSON)
public class NsgaIIRest extends RestResource {

	@Override
	public String getQueueName() {
		return NsgaII.NSGAII_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
