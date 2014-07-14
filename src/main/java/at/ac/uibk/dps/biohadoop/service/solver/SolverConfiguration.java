package at.ac.uibk.dps.biohadoop.service.solver;

import java.util.List;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;
import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SolverConfiguration {

	private final String name;
	private final AlgorithmConfiguration algorithmConfiguration;
	private final Class<? extends Algorithm<?, ?>> algorithm;
	private final List<HandlerConfiguration> handlerConfigurations;

	// TODO check if builder pattern is better suited
	public SolverConfiguration(String name,
			AlgorithmConfiguration algorithmConfiguration,
			Class<? extends Algorithm<?, ?>> algorithm,
			List<HandlerConfiguration> handlerConfigurations) {
		this.name = name;
		this.algorithmConfiguration = algorithmConfiguration;
		this.algorithm = algorithm;
		this.handlerConfigurations = handlerConfigurations;
	}

	@JsonCreator
	public static SolverConfiguration create(
			@JsonProperty("name") String name,
			@JsonProperty("algorithmConfiguration") AlgorithmConfiguration algorithmConfiguration,
			@JsonProperty("algorithm") Class<? extends Algorithm<?, ?>> algorithm,
			@JsonProperty("handlerConfiguration") List<HandlerConfiguration> handlerConfigurations) {
		return new SolverConfiguration(name, algorithmConfiguration,
				algorithm, handlerConfigurations);
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

	public List<HandlerConfiguration> getHandlerConfigurations() {
		return handlerConfigurations;
	}
}
