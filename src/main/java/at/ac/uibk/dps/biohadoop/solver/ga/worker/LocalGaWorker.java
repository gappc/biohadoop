package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class LocalGaWorker implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalGaWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############",
				LocalGaWorker.class.getSimpleName());
		TaskEndpoint<int[], Double> taskEndpoint = new TaskEndpointImpl<>(Ga.GA_QUEUE);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			try {
				performanceLogger.step(LOG);

				Task<int[]> task = taskEndpoint.getTask();
				if (task == null) {
					LOG.info(
							"############# {} Worker stopped ###############",
							LocalGaWorker.class.getSimpleName());
					break;
				}
				double fitness = GaFitness.computeFitness(
						DistancesGlobal.getDistances(), task.getData());
				Task<Double> result = new Task<Double>(task.getTaskId(),
						fitness);
				taskEndpoint.putResult(result.getTaskId(), result.getData());
				Thread.sleep(0);
			} catch (InterruptedException e) {
				LOG.error("Error while running LocalGaWorker", e);
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
