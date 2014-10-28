package at.ac.uibk.dps.biohadoop.islandmodel;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.solver.SolverData;

public class RemoteDataLoader {

	private static final Logger LOG = LoggerFactory
			.getLogger(RemoteDataLoader.class);

	private static final String NO_SUITABLE_NODE = "No suitable node found";

	private final ObjectMapper objectMapper = new ObjectMapper()
			.enableDefaultTyping();

	//
	// private final Client client = ClientBuilder.newClient();
	//
	//
	//
	public SolverData<?> getSolverData(NodeData nodeData)
			throws IslandModelException {
		throw new IslandModelException("Not implemented");
		// if (nodeData == null) {
		// LOG.error(NO_SUITABLE_NODE);
		// throw new IslandModelException(NO_SUITABLE_NODE);
		// }
		//
		// String path = nodeData.getUrl() + "/" + nodeData.getSolverId()
		// + "/typed";
		//
		// try {
		// Response response = client.target(path)
		// .request(MediaType.APPLICATION_JSON).get();
		//
		// String dataString = response.readEntity(String.class);
		// SolverData<?> solverData = objectMapper.readValue(dataString,
		// SolverData.class);
		// if (solverData == null) {
		// LOG.error("No remote data found at {}", path);
		// throw new IslandModelException("No remote data found at "
		// + path);
		// } else {
		// return solverData;
		// }
		// } catch (ProcessingException e) {
		// LOG.error("Could not get remote data from {}", path, e);
		// throw new IslandModelException(e);
		// } catch (IOException e) {
		// LOG.error("Error while deserialization of resource {}", path, e);
		// throw new IslandModelException(e);
		// }
	}

	public List<SolverData<?>> getSolverDatas(List<NodeData> nodesData)
			throws IslandModelException {
		throw new IslandModelException("Not implemented");
		// if (nodesData == null || nodesData.isEmpty()) {
		// LOG.error(NO_SUITABLE_NODE);
		// throw new IslandModelException(NO_SUITABLE_NODE);
		// }
		//
		// List<SolverData<?>> solverDatas = new ArrayList<>();
		// for (NodeData nodeData : nodesData) {
		// String path = nodeData.getUrl() + "/" + nodeData.getSolverId()
		// + "/typed";
		//
		// try {
		// Response response = client.target(path)
		// .request(MediaType.APPLICATION_JSON).get();
		//
		// String dataString = response.readEntity(String.class);
		// SolverData<?> solverData = objectMapper.readValue(dataString,
		// SolverData.class);
		// if (solverData == null) {
		// LOG.error("No remote data found at {}", path);
		// } else {
		// solverDatas.add(solverData);
		// }
		// } catch (ProcessingException e) {
		// LOG.error("Could not get remote data from {}", path, e);
		// throw new IslandModelException(e);
		// } catch (IOException e) {
		// LOG.error("Error while deserialization of resource {}", path, e);
		// throw new IslandModelException(e);
		// }
		// }
		// return solverDatas;
	}
}
