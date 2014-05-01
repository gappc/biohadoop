package at.ac.uibk.dps.biohadoop.ga.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.hadoop.AbstractConfig;

/**
 * @author Christian Gapp
 * 
 */
public class GaConfig extends AbstractConfig {

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
	private GaAlgorithmConfig algorithmConfig = new GaAlgorithmConfig();

	public GaConfig() {
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

	public GaAlgorithmConfig getAlgorithmConfig() {
		return algorithmConfig;
	}

	public void setAlgorithmConfig(GaAlgorithmConfig algorithmConfig) {
		this.algorithmConfig = algorithmConfig;
	}
}
