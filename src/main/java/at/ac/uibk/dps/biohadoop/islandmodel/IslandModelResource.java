package at.ac.uibk.dps.biohadoop.islandmodel;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;

@Path(IslandModelResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class IslandModelResource {

	public static final String PATH = "islandmodel";

	private static final Logger LOG = LoggerFactory
			.getLogger(IslandModelResource.class);
	private static final Map<SolverId, Object> DATA_FOR_SOLVER = new ConcurrentHashMap<>();

	public static void publish(SolverId solverId, Object data) {
		DATA_FOR_SOLVER.put(solverId, data);
	}

	@GET
	@Path("{solverId}")
	public Response getSolverData(@PathParam("solverId") SolverId solverId) {
		Object data = DATA_FOR_SOLVER.get(solverId);
		if (data == null) {
			return Response.noContent().build();
		} else {
			try {
				String result = JsonMapper.OBJECT_MAPPER
						.writeValueAsString(data);
				return Response.ok(result).build();
			} catch (IOException e) {
				LOG.error("Could not convert Object {}", data, e);
				return Response.serverError().build();
			}
			
		}
	}
}
