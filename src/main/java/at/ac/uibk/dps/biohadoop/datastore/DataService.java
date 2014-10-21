package at.ac.uibk.dps.biohadoop.datastore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class DataService {

	private static final Logger LOG = LoggerFactory
			.getLogger(DataService.class);

	private static final DataService DATA_SERVICE = new DataService();
	private Map<SolverId, Map<Option<?>, Object>> dataForSolver = new ConcurrentHashMap<>();
	private Object monitor = new Object();

	private DataService() {
	}

	public static DataService getInstance() {
		return DataService.DATA_SERVICE;
	}

	public <T> void setData(SolverId solverId, Option<T> option, T data) {
		if (data == null) {
			LOG.debug("Can not store null, therefor storing nothing");
			return;
		}
		
		Map<Option<?>, Object> dataStore = null;
		synchronized (monitor) {
			dataStore = dataForSolver.get(solverId);
			if (dataStore == null) {
				dataStore = new ConcurrentHashMap<>();
				dataForSolver.put(solverId, dataStore);
			}
		}
		dataStore.put(option, data);
	}

	public <T> T getData(SolverId solverId, Option<T> option) {
		Map<Option<?>, Object> dataStore = null;
		synchronized (monitor) {
			dataStore = dataForSolver.get(solverId);
		}
		if (dataStore != null) {
			return (T)dataStore.get(option);
		}
		LOG.warn("Could not find data for solver {} and key {}", solverId,
				option);
		return null;
	}

	public <T> boolean containsData(SolverId solverId, Option<T> option) {
		Map<Option<?>, Object> dataStore = null;
		synchronized (monitor) {
			dataStore = dataForSolver.get(solverId);
		}
		if (dataStore != null) {
			return dataStore.containsKey(option);
		}
		return false;
	}
}
