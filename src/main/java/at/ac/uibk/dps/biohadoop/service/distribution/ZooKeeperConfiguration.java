package at.ac.uibk.dps.biohadoop.service.distribution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZooKeeperConfiguration {

	private final String host;
	private final int port;

	public ZooKeeperConfiguration(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@JsonCreator
	public static ZooKeeperConfiguration create(
			@JsonProperty("host") String host,
			@JsonProperty("port") int port) {
		return new ZooKeeperConfiguration(host, port);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
