package at.ac.uibk.dps.biohadoop.islandmodel.zookeeper;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.tasksystem.algorithm.AlgorithmId;

public class NodeData {

	private final String url;
	private final AlgorithmId algorithmId;

	public NodeData(AlgorithmId algorithmId, String url) {
		this.algorithmId = algorithmId;
		this.url = url;
	}

	@JsonCreator
	public static NodeData create(
			@JsonProperty("algorithmId") AlgorithmId algorithmId,
			@JsonProperty("url") String url) {
		return new NodeData(algorithmId, url);
	}

	public String getUrl() {
		return url;
	}

	public AlgorithmId getAlgorithmId() {
		return algorithmId;
	}

}
