package at.ac.uibk.dps.biohadoop.islandmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.islandmodel.zookeeper.NodeData;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RemoteDataLoader<T> {

	private static final Logger LOG = LoggerFactory
			.getLogger(RemoteDataLoader.class);

	private static final String NO_SUITABLE_NODE = "No suitable node found";
	private static final Client client = Client.create();

	public T getRemoteData(NodeData nodeData) throws IslandModelException {
		if (nodeData == null) {
			LOG.error(NO_SUITABLE_NODE);
			throw new IslandModelException(NO_SUITABLE_NODE);
		}

		String path = nodeData.getUrl() + "/" + nodeData.getSolverId();

		WebResource webResource = client.resource(path);
		ClientResponse response = webResource.accept("application/json").get(
				ClientResponse.class);
		String output = response.getEntity(String.class);
		try {
			T data = JsonMapper.OBJECT_MAPPER.readValue(output,
					new TypeReference<T>() {
					});
			if (data == null) {
				LOG.error("No remote data found at {}", path);
				throw new IslandModelException("No remote data found at "
						+ path);
			} else {
				return data;
			}
		} catch (IOException e) {
			throw new IslandModelException(e);
		}
	}

	public List<T> getRemoteDatas(List<NodeData> nodesData)
			throws IslandModelException {
		if (nodesData == null || nodesData.isEmpty()) {
			LOG.error(NO_SUITABLE_NODE);
			throw new IslandModelException(NO_SUITABLE_NODE);
		}

		List<T> datas = new ArrayList<>();
		for (NodeData nodeData : nodesData) {
			String path = nodeData.getUrl() + "/" + nodeData.getSolverId();

			WebResource webResource = client.resource(path);
			ClientResponse response = webResource.accept("application/json")
					.get(ClientResponse.class);
			String output = response.getEntity(String.class);
			try {
				T data = JsonMapper.OBJECT_MAPPER.readValue(output,
						new TypeReference<T>() {
						});
				if (data == null) {
					LOG.error("No remote data found at {}", path);
					throw new IslandModelException("No remote data found at "
							+ path);
				} else {
					datas.add(data);
				}
			} catch (IOException e) {
				throw new IslandModelException(e);
			}
		}
		return datas;
	}
}
