package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	private final String version;
	private final List<String> includePaths;
	private final List<ApplicationConfiguration> applicationConfigs;
	private final ConnectionConfiguration connectionConfiguration;
	private final DistributionConfiguration distributionConfiguration;

	public BiohadoopConfiguration(String version, List<String> includePaths,
			List<ApplicationConfiguration> applicationConfigs,
			ConnectionConfiguration connectionConfiguration,
			DistributionConfiguration distributionConfiguration) {
		super();
		this.version = version;
		this.includePaths = includePaths;
		this.applicationConfigs = applicationConfigs;
		this.connectionConfiguration = connectionConfiguration;
		this.distributionConfiguration = distributionConfiguration;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("version") String version,
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("applicationConfigs") List<ApplicationConfiguration> applicationConfigs,
			@JsonProperty("connectionConfiguration") ConnectionConfiguration connectionConfiguration,
			@JsonProperty("distributionConfiguration") DistributionConfiguration distributionConfiguration) {
		return new BiohadoopConfiguration(version, includePaths,
				applicationConfigs, connectionConfiguration,
				distributionConfiguration);
	}

	public String getVersion() {
		return version;
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public List<ApplicationConfiguration> getApplicationConfigs() {
		return applicationConfigs;
	}

	public ConnectionConfiguration getConnectionConfiguration() {
		return connectionConfiguration;
	}

	public DistributionConfiguration getDistributionConfiguration() {
		return distributionConfiguration;
	}

}
