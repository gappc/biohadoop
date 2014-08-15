package at.ac.uibk.dps.biohadoop.solver;

import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SolverConfiguration {

	private final String name;
	private final Class<? extends Algorithm> algorithm;
	private final Map<String, String> properties;

	// TODO check if builder pattern is better suited
	public SolverConfiguration(String name,
			Class<? extends Algorithm> algorithm,
			Map<String, String> properties) {
		this.name = name;
		this.algorithm = algorithm;
		this.properties = properties;
	}

	@JsonCreator
	public static SolverConfiguration create(@JsonProperty("name") String name,
			@JsonProperty("algorithm") Class<? extends Algorithm> algorithm,
			@JsonProperty("properties") Map<String, String> properties) {
		return new SolverConfiguration(name, algorithm, properties);
	}

	public String getName() {
		return name;
	}

	public Class<? extends Algorithm> getAlgorithm() {
		return algorithm;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public static class Builder {
		private String algorithmName;
		private Class<? extends Algorithm> algorithmClass;
		private Map<String, String> properties = new HashMap<>();

		public Builder(String algorithmName, Class<? extends Algorithm> algorithmClass) {
			this.algorithmName = algorithmName;
			this.algorithmClass = algorithmClass;
		}

		public Builder addProperty(String key, String value) {
			properties.put(key, value);
			return this;
		}

		public SolverConfiguration build() {
			return new SolverConfiguration(algorithmName, algorithmClass, properties);
		}
	}
}
