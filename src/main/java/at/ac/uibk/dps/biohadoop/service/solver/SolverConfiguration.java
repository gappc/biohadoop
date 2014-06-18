package at.ac.uibk.dps.biohadoop.service.solver;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.service.distribution.DistributionConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.file.FilePersistenceConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SolverConfiguration {

	private final String name;
	private final AlgorithmConfiguration algorithmConfiguration;
	private final Class<? extends Algorithm<?, ?>> algorithm;
	private final PersistenceConfiguration persistenceConfiguration;
	private final DistributionConfiguration distributionConfiguration;

	// TODO check if builder pattern is better suited
	public SolverConfiguration(String name,
			AlgorithmConfiguration algorithmConfiguration,
			Class<? extends Algorithm<?, ?>> algorithm,
			PersistenceConfiguration persistenceConfiguration,
			DistributionConfiguration distributionConfiguration) {
		this.name = name;
		this.algorithmConfiguration = algorithmConfiguration;
		this.algorithm = algorithm;
		this.persistenceConfiguration = persistenceConfiguration;
		this.distributionConfiguration = distributionConfiguration;
	}

	@JsonCreator
	public static SolverConfiguration create(
			@JsonProperty("name") String name,
			@JsonProperty("algorithmConfiguration") AlgorithmConfiguration algorithmConfiguration,
			@JsonProperty("algorithm") Class<? extends Algorithm<?, ?>> algorithm,
			@JsonProperty("filePersistence") FilePersistenceConfiguration filePersistence,
			@JsonProperty("distributionConfiguration") DistributionConfiguration distributionConfiguration) {
		return new SolverConfiguration(name, algorithmConfiguration,
				algorithm, filePersistence, distributionConfiguration);
	}

	public String getName() {
		return name;
	}

	public AlgorithmConfiguration getAlgorithmConfiguration() {
		return algorithmConfiguration;
	}

	public Class<? extends Algorithm<?, ?>> getAlgorithm() {
		return algorithm;
	}

	public PersistenceConfiguration getPersistenceConfiguration() {
		return persistenceConfiguration;
	}

	public DistributionConfiguration getDistributionConfiguration() {
		return distributionConfiguration;
	}
}
