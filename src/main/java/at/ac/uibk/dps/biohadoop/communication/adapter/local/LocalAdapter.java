package at.ac.uibk.dps.biohadoop.communication.adapter.local;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerException;

public class LocalAdapter implements Adapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalAdapter.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private LocalWorker<?, ?, ?> localWorker;

	@Override
	public void configure(String settingName)
			throws AdapterException {
		localWorker = new LocalWorker<>();
		try {
			localWorker.configure(new String[] { settingName });
		} catch (WorkerException e) {
			throw new AdapterException("Could not configure local worker", e);
		}
	}

	@Override
	public void start() throws AdapterException {
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
