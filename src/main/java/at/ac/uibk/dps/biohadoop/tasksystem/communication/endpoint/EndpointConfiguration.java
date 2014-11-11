package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class EndpointConfiguration {

	private final Class<? extends Endpoint> endpoint;

	public EndpointConfiguration(Class<? extends Endpoint> endpoint) {
		this.endpoint = endpoint;
	}

	@JsonCreator
	public static EndpointConfiguration create(
			@JsonProperty("endpoint") Class<? extends Endpoint> endpoint) {
		return new EndpointConfiguration(endpoint);
	}

	public Class<? extends Endpoint> getEndpoint() {
		return endpoint;
	}

	@Override
	public String toString() {
		String endpointClass = endpoint != null ? endpoint.getCanonicalName() : null;
		return "endpoint=" + endpointClass;
	}

}
