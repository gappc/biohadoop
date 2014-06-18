package at.ac.uibk.dps.biohadoop.service.distribution.zooKeeper;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
