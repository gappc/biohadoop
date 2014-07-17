package at.ac.uibk.dps.biohadoop.communication.master.local2;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorkerAnnotation;
import at.ac.uibk.dps.biohadoop.communication.worker.SuperLocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.SuperWorker;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;

public class LocalSuperEndpoint implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalSuperEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<SuperLocalWorker<?, ?>> localWorkers = new ArrayList<>();
	private final Class<? extends SuperWorker<?, ?>> localWorker;
	private final String queueName;
	private final Object registrationObject;

	public LocalSuperEndpoint(Class<? extends SuperWorker<?, ?>> localWorker,
			String queueName, Object registrationObject) {
		this.localWorker = localWorker;
		this.queueName = queueName;
		this.registrationObject = registrationObject;
	}

	@Override
	public void configure() {
		Annotation annotation = localWorker
				.getAnnotation(LocalWorkerAnnotation.class);
		if (annotation != null) {
			BiohadoopConfiguration biohadoopConfiguration = Environment
					.getBiohadoopConfiguration();
			CommunicationConfiguration communicationConfiguration = biohadoopConfiguration
					.getCommunicationConfiguration();

			Integer workerCount = communicationConfiguration.getWorkers().get(
					localWorker);

			for (int i = 0; i < workerCount; i++) {
				localWorkers.add(new SuperLocalWorker(localWorker, queueName,
						registrationObject));
			}
		}
	}

	@Override
	public void start() throws EndpointLaunchException {
		for (SuperLocalWorker<?, ?> localGaWorker : localWorkers) {
			executorService.submit(localGaWorker);
		}
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		for (SuperLocalWorker<?, ?> localGaWorker : localWorkers) {
			localGaWorker.stop();
		}
		executorService.shutdown();
	}

}
