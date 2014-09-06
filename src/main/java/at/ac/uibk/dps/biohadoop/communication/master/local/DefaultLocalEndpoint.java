package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerException;

public class DefaultLocalEndpoint implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultLocalEndpoint.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private DefaultLocalWorker<?, ?, ?> localWorker;

	@Override
	public void configure(String settingName)
			throws MasterException {
		localWorker = new DefaultLocalWorker<>();
		try {
			localWorker.configure(new String[] { settingName });
		} catch (WorkerException e) {
			throw new MasterException("Could not configure local worker", e);
		}
	}

	@Override
	public void start() throws MasterException {
		// TODO must catch WorkerExecption
		executorService.submit(localWorker);
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		localWorker.stop();
		executorService.shutdown();
	}

}
