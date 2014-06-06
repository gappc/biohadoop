package at.ac.uibk.dps.biohadoop.applicationmanager;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;

public class ApplicationConfiguration {

	private String name;
	private AlgorithmConfiguration algorithmConfiguration;
	private Class<? extends Algorithm<?, ?>> algorithm;

	public ApplicationConfiguration() {
	}
	
	public ApplicationConfiguration(String name, AlgorithmConfiguration algorithmConfiguration,
			Class<? extends Algorithm<?, ?>> algorithm) {
		this.name = name;
		this.algorithmConfiguration = algorithmConfiguration;
		this.algorithm = algorithm;
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
}
