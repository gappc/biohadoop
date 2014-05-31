package at.ac.uibk.dps.biohadoop.ga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class LocalGaWorker implements Runnable, ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalGaWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOG.info("############# {} started ##############",
				LocalGaWorker.class.getSimpleName());
		JobManager<int[], Double> jobManager = JobManager.getInstance();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			try {
				performanceLogger.step(LOG);

				Task<int[]> task = jobManager.getTask(Ga.GA_QUEUE);
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
				jobManager.putResult(result, Ga.GA_QUEUE);
				Thread.sleep(0);
			} catch (InterruptedException e) {
				LOG.error("Error while running LocalGaWorker", e);
			}
		}
	}

	@Override
	public void shutdown() {
		synchronized (stop) {
			stop = true;
		}
	}

}
