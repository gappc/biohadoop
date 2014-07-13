package at.ac.uibk.dps.biohadoop.solver.nsgaii.master.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.worker.LocalNsgaIIWorker;

public class NsgaIILocal implements MasterConnection {

	private static final Logger LOG = LoggerFactory.getLogger(NsgaIILocal.class);
	
	private final String workerClass = LocalNsgaIIWorker.class.getCanonicalName();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<LocalNsgaIIWorker> localNsgaIIWorkers = new ArrayList<>();

	@Override
	public void configure() {
		BiohadoopConfiguration biohadoopConfiguration = Environment.getBiohadoopConfiguration();
		ConnectionConfiguration connectionConfiguration = biohadoopConfiguration.getConnectionConfiguration();
		Integer workerCount = connectionConfiguration.getWorkerEndpoints().get(workerClass);
		if (workerCount != null) {
			for (int i = 0; i < workerCount; i++) {
				localNsgaIIWorkers.add(new LocalNsgaIIWorker());
			}
		}
	}

	@Override
	public void start() throws EndpointLaunchException {
		try {
			executorService.invokeAll(localNsgaIIWorkers);
		} catch (InterruptedException e) {
			LOG.error("Error while running {}", workerClass);
			throw new EndpointLaunchException(e);
		}
	}

	@Override
	public void stop() {
		for (LocalNsgaIIWorker localNsgaIIWorker : localNsgaIIWorkers) {
			localNsgaIIWorker.stop();
		}
		executorService.shutdown();
	}

}
