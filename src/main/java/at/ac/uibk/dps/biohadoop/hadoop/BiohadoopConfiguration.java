package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.service.distribution.ZooKeeperConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	private final String version;
	private final List<String> includePaths;
	private final List<SolverConfiguration> solverConfiguration;
	private final ConnectionConfiguration connectionConfiguration;
	private final ZooKeeperConfiguration zooKeeperConfiguration;

	public BiohadoopConfiguration(String version, List<String> includePaths,
			List<SolverConfiguration> solverConfiguration,
			ConnectionConfiguration connectionConfiguration,
			ZooKeeperConfiguration zooKeeperConfiguration) {
		super();
		this.version = version;
		this.includePaths = includePaths;
		this.solverConfiguration = solverConfiguration;
		this.connectionConfiguration = connectionConfiguration;
		this.zooKeeperConfiguration = zooKeeperConfiguration;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("version") String version,
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("solverConfiguration") List<SolverConfiguration> solverConfiguration,
			@JsonProperty("connectionConfiguration") ConnectionConfiguration connectionConfiguration,
			@JsonProperty("zooKeeperConfiguration") ZooKeeperConfiguration zooKeeperConfiguration) {
		return new BiohadoopConfiguration(version, includePaths,
				solverConfiguration, connectionConfiguration,
				zooKeeperConfiguration);
	}

	public String getVersion() {
		return version;
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public List<SolverConfiguration> getSolverConfiguration() {
		return solverConfiguration;
	}

	public ConnectionConfiguration getConnectionConfiguration() {
		return connectionConfiguration;
	}

	public ZooKeeperConfiguration getZooKeeperConfiguration() {
		return zooKeeperConfiguration;
	}

}
