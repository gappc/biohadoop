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

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;

@Path(IslandModelResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class IslandModelResource {

	public static final String PATH = "islandmodel";

	private static final Logger LOG = LoggerFactory
			.getLogger(IslandModelResource.class);
	private static final Map<AlgorithmId, Object> DATA_FOR_ALLGORITHM = new ConcurrentHashMap<>();

	public static void publish(AlgorithmId algorithmId, Object data) {
		DATA_FOR_ALLGORITHM.put(algorithmId, data);
	}

	@GET
	@Path("{algorithmId}")
	public Response getAlgorithmData(@PathParam("algorithmId") AlgorithmId algorithmId) {
		Object data = DATA_FOR_ALLGORITHM.get(algorithmId);
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
