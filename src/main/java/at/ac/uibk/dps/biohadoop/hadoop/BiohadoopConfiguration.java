package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.distributionmanager.GlobalDistributionConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BiohadoopConfiguration {

	private final String version;
	private final List<String> includePaths;
	private final List<ApplicationConfiguration> applicationConfigs;
	private final ConnectionConfiguration connectionConfiguration;
	private final GlobalDistributionConfiguration globalDistributionConfiguration;

	public BiohadoopConfiguration(String version, List<String> includePaths,
			List<ApplicationConfiguration> applicationConfigs,
			ConnectionConfiguration connectionConfiguration,
			GlobalDistributionConfiguration globalDistributionConfiguration) {
		super();
		this.version = version;
		this.includePaths = includePaths;
		this.applicationConfigs = applicationConfigs;
		this.connectionConfiguration = connectionConfiguration;
		this.globalDistributionConfiguration = globalDistributionConfiguration;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("version") String version,
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("applicationConfigs") List<ApplicationConfiguration> applicationConfigs,
			@JsonProperty("connectionConfiguration") ConnectionConfiguration connectionConfiguration,
			@JsonProperty("globalDistributionConfiguration") GlobalDistributionConfiguration globalDistributionConfiguration) {
		return new BiohadoopConfiguration(version, includePaths,
				applicationConfigs, connectionConfiguration,
				globalDistributionConfiguration);
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

	public GlobalDistributionConfiguration getGlobalDistributionConfiguration() {
		return globalDistributionConfiguration;
	}

}
