package at.ac.uibk.dps.biohadoop.applicationmanager;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.file.FilePersistenceConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationConfiguration {

	private final String name;
	private final AlgorithmConfiguration algorithmConfiguration;
	private final Class<? extends Algorithm<?, ?>> algorithm;
	private final PersistenceConfiguration persistenceConfiguration;
	
	public ApplicationConfiguration(String name, AlgorithmConfiguration algorithmConfiguration,
			Class<? extends Algorithm<?, ?>> algorithm, PersistenceConfiguration persistenceConfiguration) {
		this.name = name;
		this.algorithmConfiguration = algorithmConfiguration;
		this.algorithm = algorithm;
		this.persistenceConfiguration = persistenceConfiguration;
	}

	@JsonCreator
	public static ApplicationConfiguration create(
			@JsonProperty("name") String name,
			@JsonProperty("algorithmConfiguration") AlgorithmConfiguration algorithmConfiguration,
			@JsonProperty("algorithm") Class<? extends Algorithm<?, ?>> algorithm,
			@JsonProperty("filePersistence") FilePersistenceConfiguration filePersistence) {
		return new ApplicationConfiguration(name, algorithmConfiguration, algorithm, filePersistence);
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
}
