package at.ac.uibk.dps.biohadoop.solver.ga.master.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.connection.rest.RestResource;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRest extends RestResource {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public String getPrefix() {
		return "GA";
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}
}
