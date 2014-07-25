package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker2;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;

public abstract class LocalEndpoint implements MasterLifecycle, MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<LocalWorker2<?, ?>> localWorkers = new ArrayList<>();

	@Override
	public void configure() {
//		BiohadoopConfiguration biohadoopConfiguration = Environment
//				.getBiohadoopConfiguration();
//		CommunicationConfiguration communicationConfiguration = biohadoopConfiguration
//				.getCommunicationConfiguration();
//		String localWorkerClass = getWorkerClass().getCanonicalName();
//		Integer workerCount = communicationConfiguration.getWorkerEndpoints()
//				.get(localWorkerClass);
//		if (workerCount != null) {
//			try {
//				for (int i = 0; i < workerCount; i++) {
//					LocalWorker<?, ?> localWorker = (LocalWorker<?, ?>) getWorkerClass()
//							.newInstance();
//					// localWorker.setRegistrationObject(getRegistrationObject());
//					localWorkers.add(localWorker);
//				}
//			} catch (InstantiationException | IllegalAccessException e) {
//				LOG.error("Could not instanciate {}", getWorkerClass(), e);
//				localWorkers.clear();
//			}
//		}
	}

	@Override
	public void start() throws EndpointLaunchException {
		for (LocalWorker2<?, ?> localGaWorker : localWorkers) {
			executorService.submit(localGaWorker);
		}
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		for (LocalWorker2<?, ?> localGaWorker : localWorkers) {
			localGaWorker.stop();
		}
		executorService.shutdown();
	}

	public abstract Class<? extends LocalWorker2<?, ?>> getWorkerClass();

}
