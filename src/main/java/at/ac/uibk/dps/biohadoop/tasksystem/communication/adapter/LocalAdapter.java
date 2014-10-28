package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerException;

public class LocalAdapter implements Adapter {
	@Override
	public Class<? extends Worker> getMatchingWorkerClass() {
		// TODO Auto-generated method stub
		return null;
	}
	private static final Logger LOG = LoggerFactory
			.getLogger(LocalAdapter.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private LocalWorker<?, ?, ?> localWorker;

//	@Override
//	public void configure(String pipelineName)
//			throws AdapterException {
//		localWorker = new LocalWorker<>();
//		try {
//			localWorker.configure(new String[] { pipelineName });
//		} catch (WorkerException e) {
//			throw new AdapterException("Could not configure local worker", e);
//		}
//	}

	@Override
	public void start(String pipelineName) throws AdapterException {
		// TODO must catch WorkerExecption
		executorService.submit(localWorker);
		LOG.info("Local Workers started");
	}

	@Override
	public void stop() {
		localWorker.stop();
		executorService.shutdown();
	}

//	@Override
//	public int getPort(String pipelineName) throws AdapterException {
//		// TODO Auto-generated method stub
//		return 0;
//	}

}
