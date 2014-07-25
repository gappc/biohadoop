package at.ac.uibk.dps.biohadoop.communication.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public abstract class LocalWorker2<T, S> implements Callable<Integer>,
		WorkerEndpoint<T, S> {//, WorkerParameter {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalWorker2.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(LocalWorker2.class);

	private final AtomicBoolean stop = new AtomicBoolean(false);

	private int logSteps = 1000;

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############", CLASSNAME);
		MasterEndpoint masterEndpoint;
		try {
			masterEndpoint = getMasterEndpoint().newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			LOG.error("Could not instanciate MasterEndpoint {}",
					getMasterEndpoint());
			return 1;
		}

		TaskEndpoint<T, S> taskEndpoint = new TaskEndpointImpl<>(
				masterEndpoint.getQueueName());
		boolean registrationInit = false;

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (!stop.get()) {
			try {
				performanceLogger.step(LOG);

				Task<T> task = taskEndpoint.getTask();
				if (task == null) {
					LOG.info("############# {} Worker stopped ###############",
							CLASSNAME);
					break;
				}
				if (!registrationInit) {
					doRegistrationInit(masterEndpoint);
					registrationInit = true;
				}
				S data = compute(task.getData());
				taskEndpoint.putResult(task.getTaskId(), data);
			} catch (InterruptedException e) {
				LOG.debug("Got InterruptedException, stopping work");
			}
		}
		return 0;
	}

	public void stop() {
		stop.set(true);
	}

//	@Override
//	public String getWorkerParameters() throws Exception {
//		LOG.error("getWorkerParameters");
//		return null;
//	}

	@Override
	public void run(String host, int port) throws WorkerException {
		LOG.error("run");
	}

	private void doRegistrationInit(MasterEndpoint masterEndpoint) {
		Object registrationObject = masterEndpoint.getRegistrationObject();
		readRegistrationObject(registrationObject);
	}

}
