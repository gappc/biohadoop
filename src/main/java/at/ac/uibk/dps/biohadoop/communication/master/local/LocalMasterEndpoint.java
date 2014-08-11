package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.worker.UnifiedLocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerException;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointConfigureException;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLaunchException;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public class LocalMasterEndpoint implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalMasterEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<UnifiedLocalWorker<?, ?, ?>> localWorkers = new ArrayList<>();

	@Override
	public void configure(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws EndpointConfigureException {
		UnifiedLocalWorker<?, ?, ?> localWorker = new UnifiedLocalWorker<>();
		try {
			localWorker.configure(new String[] { "",
					remoteExecutable.getCanonicalName(), "", "0" });
			localWorkers.add(new UnifiedLocalWorker<>());
		} catch (WorkerException e) {
			throw new EndpointConfigureException("Could not configure local worker", e);
		}
		
		// BiohadoopConfiguration biohadoopConfiguration = Environment
		// .getBiohadoopConfiguration();
		// CommunicationConfiguration communicationConfiguration =
		// biohadoopConfiguration
		// .getCommunicationConfiguration();
		//
		// Integer workerCount = communicationConfiguration.getWorkers().get(
		// remoteExecutable);
		//
		// if (workerCount != null) {
		// try {
		// for (int i = 0; i < workerCount; i++) {
		// localWorkers.add(new UnifiedLocalWorker<>(remoteExecutable
		// .getCanonicalName()));
		// }
		// } catch (WorkerException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// throw new EndpointConfigureException(e);
		// }
		// } else {
		// LOG.warn("No workers for local running solver found, therefor no local workers started");
		// }
	}

	@Override
	public void start() throws EndpointLaunchException {
		for (UnifiedLocalWorker<?, ?, ?> localGaWorker : localWorkers) {
			executorService.submit(localGaWorker);
		}
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		for (UnifiedLocalWorker<?, ?, ?> localGaWorker : localWorkers) {
			localGaWorker.stop();
		}
		executorService.shutdown();
	}

}
