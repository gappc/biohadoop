package at.ac.uibk.dps.biohadoop.performance.test.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.hadoop.AbstractConfig;

public class PerformanceConfig extends AbstractConfig {

	/**
	 * List of classnames for endpoints, that YARN appliation master should
	 * provide
	 */
	private List<String> masterEndpoints = new ArrayList<String>();

	/**
	 * Map that contains definition of workers and their number
	 */
	private Map<String, Integer> workers = new HashMap<String, Integer>();

	/**
	 * Special configurations for the GA-TSP algorithm
	 */
	private PerformanceAlgorithmConfig algorithmConfig = new PerformanceAlgorithmConfig();

	public PerformanceConfig() {
	}
	
	public List<String> getMasterEndpoints() {
		return masterEndpoints;
	}

	public void setMasterEndpoints(List<String> masterEndpoints) {
		this.masterEndpoints = masterEndpoints;
	}

	public Map<String, Integer> getWorkers() {
		return workers;
	}

	public void setWorkers(Map<String, Integer> workers) {
		this.workers = workers;
	}

	public PerformanceAlgorithmConfig getAlgorithmConfig() {
		return algorithmConfig;
	}

	public void setAlgorithmConfig(PerformanceAlgorithmConfig algorithmConfig) {
		this.algorithmConfig = algorithmConfig;
	}
}