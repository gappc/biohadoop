package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;


//TODO: NOT working
public abstract class LocalWorker<T, S> implements WorkerEndpoint<T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;
	public void stop() {
		synchronized (stop) {
			stop = true;
		}
	}

	@Override
	public void run(String host, int port) throws Exception {
//		TODO QUEUENAME
		TaskEndpoint<T, S> taskEndpoint = new TaskEndpointImpl<>(Ga.GA_QUEUE);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			try {
				performanceLogger.step(LOG);

				Task<T> task = taskEndpoint.getTask();
				if (task == null) {
					LOG.info(
							"############# {} Worker stopped ###############",
							LocalWorker.class.getSimpleName());
					break;
				}
				S fitness = compute(task.getData());
				Task<S> result = new Task<S>(task.getTaskId(),
						fitness);
				taskEndpoint.putResult(result.getTaskId(), result.getData());
			} catch (InterruptedException e) {
				LOG.error("Error while running LocalGaWorker", e);
			}
		}
	}
}
