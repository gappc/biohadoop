package at.ac.uibk.dps.biohadoop.solver.ga.master.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.ac.uibk.dps.biohadoop.connection.rest.RestResource;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaEndpointConfig;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRest extends RestResource {

	public GaRest() {
		super();
		masterConfiguration = new GaEndpointConfig();
	}
}
