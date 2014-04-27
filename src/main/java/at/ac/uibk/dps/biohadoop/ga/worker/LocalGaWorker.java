package at.ac.uibk.dps.biohadoop.ga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;

public class LocalGaWorker implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LocalGaWorker.class);
	private Boolean stop = false;

	@Override
	public void run() {
		JobManager jobManager = JobManager.getInstance();
		jobManager.addObserver(this);
		while (true) {
			try {
				Task task = (Task) jobManager
						.getTaskForExecution(Ga.GA_WORK_QUEUE);

				synchronized (stop) {
					if (stop) {
						LOGGER.info("Stopping local worker {}", this);
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
