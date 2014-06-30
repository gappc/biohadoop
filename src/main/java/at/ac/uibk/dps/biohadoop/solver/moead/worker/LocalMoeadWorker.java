package at.ac.uibk.dps.biohadoop.solver.moead.worker;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class LocalMoeadWorker implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalMoeadWorker.class);

	private final String className = Helper
			.getClassname(LocalMoeadWorker.class);

	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############", className);
		TaskEndpoint<double[], double[]> taskEndpoint = new TaskEndpointImpl<>(
				Moead.MOEAD_QUEUE);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			performanceLogger.step(LOG);
			try {
				Task<double[]> task = taskEndpoint.getTask();
				if (task == null) {
					LOG.info("############# {} Worker stopped ###############",
							className);
					break;
				}

				double[] fValues = new double[2];
				fValues[0] = Functions.f1(task.getData());
				fValues[1] = Functions.f2(task.getData());

				Task<double[]> result = new Task<double[]>(task.getTaskId(),
						fValues);
				taskEndpoint.putResult(result.getTaskId(), result.getData());
				Thread.sleep(0);
			} catch (InterruptedException e) {
				LOG.error("Error while running {}", className, e);
			}
		}
		return 0;
	}

	public void stop() {
		synchronized (stop) {
			stop = true;
		}
	}

}
