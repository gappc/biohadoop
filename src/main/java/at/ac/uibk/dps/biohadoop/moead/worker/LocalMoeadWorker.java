package at.ac.uibk.dps.biohadoop.moead.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.moead.algorithm.MoeadResult;
import at.ac.uibk.dps.biohadoop.moead.algorithm.MoeadTask;

public class LocalMoeadWorker implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LocalMoeadWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOGGER.info("############# {} started ##############", LocalMoeadWorker.class.getSimpleName());
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
						.getTaskForExecution(Moead.MOEAD_WORK_QUEUE);

				synchronized (stop) {
					if (stop) {
						LOGGER.info("############# {} Worker stopped ###############", LocalMoeadWorker.class.getSimpleName());
						break;
					}
				}

				if (!(task instanceof StopTask)) {
					MoeadTask moeadTask = (MoeadTask)task;
					double[] fValues = new double[2];
					fValues[0] = Functions.f1(moeadTask.getY());
					fValues[1] = Functions.f2(moeadTask.getY());
					
					MoeadResult result = new MoeadResult(moeadTask.getId(), moeadTask.getSlot(), fValues);
					jobManager.writeResult(Moead.MOEAD_RESULT_STORE, result);
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
