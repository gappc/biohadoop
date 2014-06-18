package at.ac.uibk.dps.biohadoop.service.distribution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobalDistributionConfiguration {

	private final String host;
	private final int port;

	public GlobalDistributionConfiguration(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@JsonCreator
	public static GlobalDistributionConfiguration create(
			@JsonProperty("host") String host,
			@JsonProperty("port") int port) {
		return new GlobalDistributionConfiguration(host, port);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
