package at.ac.uibk.dps.biohadoop.service.distribution;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;

@Path("/distribution")
@Produces(MediaType.APPLICATION_JSON)
public class DistributionResource {

	@GET
	@Path("{solverId}")
	public Response getSolverData(
			@PathParam("solverId") SolverId solverId) {
		SolverService solverService = SolverService
				.getInstance();
		SolverData<?> solverData = solverService
				.getSolverData(solverId);
		if (solverData == null) {
			return Response.noContent().build();
		} else {
			return Response.ok(solverData).build();
		}
	}
}
