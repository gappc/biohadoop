package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.handler.distribution.ZooKeeperConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	private final String version;
	private final List<String> includePaths;
	private final List<SolverConfiguration> solverConfiguration;
	private final CommunicationConfiguration communicationConfiguration;
	private final ZooKeeperConfiguration zooKeeperConfiguration;

	public BiohadoopConfiguration(String version, List<String> includePaths,
			List<SolverConfiguration> solverConfiguration,
			CommunicationConfiguration communicationConfiguration,
			ZooKeeperConfiguration zooKeeperConfiguration) {
		super();
		this.version = version;
		this.includePaths = includePaths;
		this.solverConfiguration = solverConfiguration;
		this.communicationConfiguration = communicationConfiguration;
		this.zooKeeperConfiguration = zooKeeperConfiguration;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("version") String version,
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("solverConfiguration") List<SolverConfiguration> solverConfiguration,
			@JsonProperty("communicationConfiguration") CommunicationConfiguration communicationConfiguration,
			@JsonProperty("zooKeeperConfiguration") ZooKeeperConfiguration zooKeeperConfiguration) {
		return new BiohadoopConfiguration(version, includePaths,
				solverConfiguration, communicationConfiguration,
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

	public CommunicationConfiguration getCommunicationConfiguration() {
		return communicationConfiguration;
	}

	public ZooKeeperConfiguration getZooKeeperConfiguration() {
		return zooKeeperConfiguration;
	}

}
