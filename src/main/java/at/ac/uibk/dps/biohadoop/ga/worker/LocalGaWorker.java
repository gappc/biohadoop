package at.ac.uibk.dps.biohadoop.ga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;

public class LocalGaWorker implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LocalGaWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOGGER.info("############# {} started ##############", LocalGaWorker.class.getSimpleName());
		JobManager jobManager = JobManager.getInstance();
		jobManager.addObserver(this);
		
		long startTime = System.currentTimeMillis();
		int counter = 0;
		while (true) {
			try {
				counter++;
				if (counter % logSteps == 0) {
					long endTime = System.currentTimeMillis();
					LOGGER.info("{}ms for last {} computations",
							endTime - startTime, logSteps);
					startTime = System.currentTimeMillis();
					counter = 0;
				}
				
				Task task = (Task) jobManager
						.getTaskForExecution(Ga.GA_WORK_QUEUE);

				synchronized (stop) {
					if (stop) {
						LOGGER.info("############# {} Worker stopped ###############", LocalGaWorker.class.getSimpleName());
						break;
					}
				}

				if (!(task instanceof StopTask)) {
					GaTask gaTask = (GaTask) task;

					double fitness = GaFitness.computeFitness(
							DistancesGlobal.getDistances(), gaTask.getGenome());
					GaResult gaResult = new GaResult(gaTask.getSlot(), fitness);
					gaResult.setId(task.getId());
					jobManager.writeResult(Ga.GA_RESULT_STORE, gaResult);
					Thread.sleep(1);
				}
			} catch (InterruptedException e) {
				LOGGER.error("Error while running LocalGaWorker", e);
			}
		}
	}

	@Override
	public void stop() {
		synchronized (stop) {
			stop = true;
		}
	}

}
