package at.ac.uibk.dps.biohadoop.service.distribution;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.datastore.DataProvider;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/distribution")
@Produces(MediaType.APPLICATION_JSON)
public class DistributionResource {

	private static final Logger LOG = LoggerFactory
			.getLogger(DistributionResource.class);
	private static final ObjectMapper typedObjectMapper = new ObjectMapper()
			.enableDefaultTyping();

	@GET
	@Path("{solverId}")
	public Response getSolverData(@PathParam("solverId") SolverId solverId) {
		SolverData<?> solverData = DataProvider.getSolverData(solverId);
		if (solverData == null) {
			return Response.noContent().build();
		} else {
			return Response.ok(solverData).build();
		}
	}

	@GET
	@Path("{solverId}/typed")
	public Response getTypedSolverData(@PathParam("solverId") SolverId solverId) {
		SolverData<?> solverData = DataProvider.getSolverData(solverId);
		if (solverData == null) {
			return Response.noContent().build();
		} else {
			String result = null;
			try {
				result = typedObjectMapper.writeValueAsString(solverData);
			} catch (JsonProcessingException e) {
				LOG.error("Could not convert Object {}", solverData, e);
			}
			return Response.ok(result).build();
		}
	}
}
