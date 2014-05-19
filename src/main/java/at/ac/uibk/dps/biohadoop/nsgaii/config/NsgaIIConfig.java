package at.ac.uibk.dps.biohadoop.nsgaii.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.hadoop.AbstractConfig;

/**
 * @author Christian Gapp
 * 
 */
public class NsgaIIConfig extends AbstractConfig {

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
	private NsgaIIAlgorithmConfig algorithmConfig = new NsgaIIAlgorithmConfig();

	public NsgaIIConfig() {
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

	public NsgaIIAlgorithmConfig getAlgorithmConfig() {
		return algorithmConfig;
	}

	public void setAlgorithmConfig(NsgaIIAlgorithmConfig algorithmConfig) {
		this.algorithmConfig = algorithmConfig;
	}
}
