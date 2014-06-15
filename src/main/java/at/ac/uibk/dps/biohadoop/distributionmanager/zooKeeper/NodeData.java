package at.ac.uibk.dps.biohadoop.distributionmanager.zooKeeper;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeData {

	private final String url;
	private final ApplicationId applicationId;

	public NodeData(ApplicationId applicationId, String url) {
		this.applicationId = applicationId;
		this.url = url;
	}

	@JsonCreator
	public static NodeData create(
			@JsonProperty("applicationId") ApplicationId applicationId,
			@JsonProperty("url") String url) {
		return new NodeData(applicationId, url);
	}

	public String getUrl() {
		return url;
	}

	public ApplicationId getApplicationId() {
		return applicationId;
	}

}
