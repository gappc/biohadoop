package at.ac.uibk.dps.biohadoop.solver.moead.master.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.ConnectionConfiguration;
import at.ac.uibk.dps.biohadoop.connection.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;
import at.ac.uibk.dps.biohadoop.solver.moead.worker.LocalMoeadWorker;

public class MoeadLocal implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory.getLogger(MoeadLocal.class);
	
	private final String workerClass = LocalMoeadWorker.class.getCanonicalName();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<LocalMoeadWorker> localMoeadWorkers = new ArrayList<>();

	@Override
	public void configure() {
		BiohadoopConfiguration biohadoopConfiguration = Environment.getBiohadoopConfiguration();
		ConnectionConfiguration connectionConfiguration = biohadoopConfiguration.getConnectionConfiguration();
		Integer workerCount = connectionConfiguration.getWorkerEndpoints().get(workerClass);
		if (workerCount != null) {
			for (int i = 0; i < workerCount; i++) {
				localMoeadWorkers.add(new LocalMoeadWorker());
			}
		}
	}

	@Override
	public void start() throws EndpointLaunchException {
		try {
			executorService.invokeAll(localMoeadWorkers);
		} catch (InterruptedException e) {
			LOG.error("Error while running {}", workerClass);
			throw new EndpointLaunchException(e);
		}
	}

	@Override
	public void stop() {
		for (LocalMoeadWorker localMoeadWorker : localMoeadWorkers) {
			localMoeadWorker.stop();
		}
		executorService.shutdown();
	}
}
