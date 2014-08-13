package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.handler.distribution.ZooKeeperConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	public static final String CONFIG_PATH = "CONFIG_PATH";
	
	private final List<String> includePaths;
	private final List<SolverConfiguration> solverConfiguration;
	private final CommunicationConfiguration communicationConfiguration;
	private final ZooKeeperConfiguration zooKeeperConfiguration;
	private final Map<String, String> properties;

	public BiohadoopConfiguration(List<String> includePaths,
			List<SolverConfiguration> solverConfiguration,
			CommunicationConfiguration communicationConfiguration,
			ZooKeeperConfiguration zooKeeperConfiguration,
			Map<String, String> properties) {
		this.includePaths = includePaths;
		this.solverConfiguration = solverConfiguration;
		this.communicationConfiguration = communicationConfiguration;
		this.zooKeeperConfiguration = zooKeeperConfiguration;
		this.properties = properties;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("solverConfiguration") List<SolverConfiguration> solverConfiguration,
			@JsonProperty("communicationConfiguration") CommunicationConfiguration communicationConfiguration,
			@JsonProperty("zooKeeperConfiguration") ZooKeeperConfiguration zooKeeperConfiguration,
			@JsonProperty("properties") Map<String, String> properties) {
		return new BiohadoopConfiguration(includePaths, solverConfiguration,
				communicationConfiguration, zooKeeperConfiguration, properties);
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

	public Map<String, String> getProperties() {
		return properties;
	}

}
