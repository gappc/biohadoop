package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.service.distribution.GlobalDistributionConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	private final String version;
	private final List<String> includePaths;
	private final List<SolverConfiguration> solverConfigs;
	private final ConnectionConfiguration connectionConfiguration;
	private final GlobalDistributionConfiguration globalDistributionConfiguration;

	public BiohadoopConfiguration(String version, List<String> includePaths,
			List<SolverConfiguration> solverConfigs,
			ConnectionConfiguration connectionConfiguration,
			GlobalDistributionConfiguration globalDistributionConfiguration) {
		super();
		this.version = version;
		this.includePaths = includePaths;
		this.solverConfigs = solverConfigs;
		this.connectionConfiguration = connectionConfiguration;
		this.globalDistributionConfiguration = globalDistributionConfiguration;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("version") String version,
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("solverConfigs") List<SolverConfiguration> solverConfigs,
			@JsonProperty("connectionConfiguration") ConnectionConfiguration connectionConfiguration,
			@JsonProperty("globalDistributionConfiguration") GlobalDistributionConfiguration globalDistributionConfiguration) {
		return new BiohadoopConfiguration(version, includePaths,
				solverConfigs, connectionConfiguration,
				globalDistributionConfiguration);
	}

	public String getVersion() {
		return version;
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public List<SolverConfiguration> getSolverConfigs() {
		return solverConfigs;
	}

	public ConnectionConfiguration getConnectionConfiguration() {
		return connectionConfiguration;
	}

	public GlobalDistributionConfiguration getGlobalDistributionConfiguration() {
		return globalDistributionConfiguration;
	}

}
