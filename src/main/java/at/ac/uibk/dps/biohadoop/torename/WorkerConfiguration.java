package at.ac.uibk.dps.biohadoop.torename;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;

public class WorkerConfiguration {

	private Map<MasterEndpoint, List<String>> workerConfig = new HashMap<MasterEndpoint, List<String>>();
	
	public void addWorker(String identifier, List<String> workers) {
		
	}
}
