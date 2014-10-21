package at.ac.uibk.dps.biohadoop.islandmodel;


//@Path(IslandModelResource.PATH)
//@Produces(MediaType.APPLICATION_JSON)
public class IslandModelResource {
	
	public static final String PATH = "islandmodel";

//	private static final Logger LOG = LoggerFactory
//			.getLogger(IslandModelResource.class);
//	private static final ObjectMapper TYPED_OBJECT_MAPPER = new ObjectMapper()
//			.enableDefaultTyping();
//
//	@GET
//	@Path("{solverId}")
//	public Response getSolverData(@PathParam("solverId") SolverId solverId) {
//		SolverData<?> solverData = DataClient.getData(solverId, DataOptions.SOLVER_DATA);
//		if (solverData == null) {
//			return Response.noContent().build();
//		} else {
//			return Response.ok(solverData).build();
//		}
//	}
//
//	@GET
//	@Path("{solverId}/typed")
//	public Response getTypedSolverData(@PathParam("solverId") SolverId solverId) {
//		SolverData<?> solverData = DataClient.getData(solverId, DataOptions.SOLVER_DATA);
//		if (solverData == null) {
//			return Response.noContent().build();
//		} else {
//			String result = null;
//			try {
//				result = TYPED_OBJECT_MAPPER.writeValueAsString(solverData);
//			} catch (JsonProcessingException e) {
//				LOG.error("Could not convert Object {}", solverData, e);
//			}
//			return Response.ok(result).build();
//		}
//	}
}
