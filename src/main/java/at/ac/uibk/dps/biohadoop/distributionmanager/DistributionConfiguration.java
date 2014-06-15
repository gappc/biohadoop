package at.ac.uibk.dps.biohadoop.distributionmanager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionConfiguration {

	private final String host;
	private final int port;

	public DistributionConfiguration(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@JsonCreator
	public static DistributionConfiguration create(
			@JsonProperty("host") String host,
			@JsonProperty("port") int port) {
		return new DistributionConfiguration(host, port);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
