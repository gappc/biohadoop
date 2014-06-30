package at.ac.uibk.dps.biohadoop.deletable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;

public class WorkerConfiguration {

	private Map<DefaultEndpointHandler, List<String>> workerConfig = new HashMap<DefaultEndpointHandler, List<String>>();
	
	public void addWorker(String identifier, List<String> workers) {
		
	}
}
