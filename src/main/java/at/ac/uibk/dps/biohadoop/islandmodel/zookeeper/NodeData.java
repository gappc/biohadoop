package at.ac.uibk.dps.biohadoop.islandmodel.zookeeper;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class NodeData {

	private final String url;
	private final SolverId solverId;

	public NodeData(SolverId solverId, String url) {
		this.solverId = solverId;
		this.url = url;
	}

	@JsonCreator
	public static NodeData create(
			@JsonProperty("solverId") SolverId solverId,
			@JsonProperty("url") String url) {
		return new NodeData(solverId, url);
	}

	public String getUrl() {
		return url;
	}

	public SolverId getSolverId() {
		return solverId;
	}

}
