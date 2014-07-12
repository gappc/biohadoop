package at.ac.uibk.dps.biohadoop.service.distribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper.NodeData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteDataLoader {

	private final static Logger LOG = LoggerFactory
			.getLogger(RemoteDataLoader.class);

	private final ObjectMapper objectMapper = new ObjectMapper()
			.enableDefaultTyping();

	private final Client client = ClientBuilder.newClient();

	public SolverData<?> getSolverData(NodeData nodeData)
			throws DistributionException {
		if (nodeData == null) {
			LOG.error("No suitable node found");
			throw new DistributionException("No suitable node found");
		}

		String path = nodeData.getUrl() + "/" + nodeData.getSolverId()
				+ "/typed";

		try {
			Response response = client.target(path)
					.request(MediaType.APPLICATION_JSON).get();

			String dataString = response.readEntity(String.class);
			SolverData<?> solverData = objectMapper.readValue(dataString,
					SolverData.class);
			if (solverData == null) {
				LOG.error("No remote data found at {}", path);
				throw new DistributionException("No remote data found at "
						+ path);
			} else {
				return solverData;
			}
		} catch (ProcessingException e) {
			LOG.error("Could not get remote data from {}", path, e);
			throw new DistributionException(e);
		} catch (IOException e) {
			LOG.error("Error while deserialization of resource {}", path, e);
			throw new DistributionException(e);
		}
	}

	public List<SolverData<?>> getSolverDatas(List<NodeData> nodesData)
			throws DistributionException {
		if (nodesData == null || nodesData.size() == 0) {
			LOG.error("No suitable node found");
			throw new DistributionException("No suitable node found");
		}

		List<SolverData<?>> solverDatas = new ArrayList<>();
		for (NodeData nodeData : nodesData) {
			String path = nodeData.getUrl() + "/" + nodeData.getSolverId()
					+ "/typed";

			try {
				Response response = client.target(path)
						.request(MediaType.APPLICATION_JSON).get();

				String dataString = response.readEntity(String.class);
				SolverData<?> solverData = objectMapper.readValue(dataString,
						SolverData.class);
				if (solverData == null) {
					LOG.error("No remote data found at {}", path);
				} else {
					solverDatas.add(solverData);
				}
			} catch (ProcessingException e) {
				LOG.error("Could not get remote data from {}", path, e);
				throw new DistributionException(e);
			} catch (IOException e) {
				LOG.error("Error while deserialization of resource {}", path, e);
				throw new DistributionException(e);
			}
		}
		return solverDatas;
	}
}
