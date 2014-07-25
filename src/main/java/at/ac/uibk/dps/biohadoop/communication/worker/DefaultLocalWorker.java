package at.ac.uibk.dps.biohadoop.communication.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.local.LocalMaster;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class DefaultLocalWorker<T, S> implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultLocalWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(DefaultLocalWorker.class);

	private final Worker<T, S> worker;
	private final AtomicBoolean stop = new AtomicBoolean(false);

	private int logSteps = 1000;

	public DefaultLocalWorker(Class<? extends Worker<T, S>> workerClass)
			throws InstantiationException, IllegalAccessException {
		worker = workerClass.newInstance();
	}

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############", CLASSNAME);

		String queueName = worker.getClass()
				.getAnnotation(LocalWorker.class).master()
				.getAnnotation(LocalMaster.class).queueName();
		TaskEndpoint<T, S> taskEndpoint = new TaskEndpointImpl<>(queueName);
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
					doRegistrationInit();
					registrationInit = true;
				}
				S data = worker.compute(task.getData());
				taskEndpoint.putResult(task.getTaskId(), data);
			} catch (InterruptedException e) {
				LOG.debug("Got InterruptedException, stopping work");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	public void stop() {
		stop.set(true);
	}

	public void run(String host, int port) throws WorkerException {
		LOG.error("run");
	}

	private void doRegistrationInit() throws InstantiationException,
			IllegalAccessException {
		Object data = worker.getClass()
				.getAnnotation(LocalWorker.class).master()
				.newInstance().getRegistrationObject();
		worker.readRegistrationObject(data);
	}

}
