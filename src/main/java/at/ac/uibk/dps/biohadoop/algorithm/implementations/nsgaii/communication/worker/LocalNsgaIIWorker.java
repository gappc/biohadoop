package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.worker;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class LocalNsgaIIWorker implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalNsgaIIWorker.class);

	private final String className = ClassnameProvider
			.getClassname(LocalNsgaIIWorker.class);

	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############",
				LocalNsgaIIWorker.class.getSimpleName());
		TaskEndpoint<double[], double[]> taskEndpoint = new TaskEndpointImpl<>(
				NsgaII.NSGAII_QUEUE);

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

				taskEndpoint.putResult(task.getTaskId(), fValues);
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
