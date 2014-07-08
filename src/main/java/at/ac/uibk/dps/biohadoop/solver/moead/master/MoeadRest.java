package at.ac.uibk.dps.biohadoop.solver.moead.master;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.connection.rest.RestResource;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

@Path("/moead")
@Produces(MediaType.APPLICATION_JSON)
public class MoeadRest extends RestResource {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
