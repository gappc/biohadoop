package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointConfigureException;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;

public class LocalMasterEndpoint implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalMasterEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<DefaultLocalWorker<?, ?>> localWorkers = new ArrayList<>();

	@Override
	public void configure(Class<? extends Master> master)
			throws EndpointConfigureException {
		LocalMaster localMasterAnnotation = master
				.getAnnotation(LocalMaster.class);
		Class<? extends Worker<?, ?>> localWorker = localMasterAnnotation
				.localWorker();
		Annotation annotation = localWorker.getAnnotation(LocalWorker.class);
		if (annotation != null) {
			BiohadoopConfiguration biohadoopConfiguration = Environment
					.getBiohadoopConfiguration();
			CommunicationConfiguration communicationConfiguration = biohadoopConfiguration
					.getCommunicationConfiguration();

			Integer workerCount = communicationConfiguration.getWorkers().get(
					localWorker);

			if (workerCount != null) {
				try {
					for (int i = 0; i < workerCount; i++) {
						localWorkers.add(new DefaultLocalWorker(localWorker));
					}
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new EndpointConfigureException(e);
				}
			} else {
				LOG.warn("No workers for local running solver found, therefor no local workers started");
			}
		}
	}

	@Override
	public void start() throws EndpointLaunchException {
		for (DefaultLocalWorker<?, ?> localGaWorker : localWorkers) {
			executorService.submit(localGaWorker);
		}
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		for (DefaultLocalWorker<?, ?> localGaWorker : localWorkers) {
			localGaWorker.stop();
		}
		executorService.shutdown();
	}

}
