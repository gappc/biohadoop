package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.job.Task;
import at.ac.uibk.dps.biohadoop.service.job.api.JobService;
import at.ac.uibk.dps.biohadoop.service.solver.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
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
		JobService<int[], Double> jobService = JobService.getInstance();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			try {
				performanceLogger.step(LOG);

				Task<int[]> task = jobService.getTask(Ga.GA_QUEUE);
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
				jobService.putResult(result, Ga.GA_QUEUE);
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
