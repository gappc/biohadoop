package at.ac.uibk.dps.biohadoop.nsgaii.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaIIResult;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaIITask;

public class LocalNsgaIIWorker implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LocalNsgaIIWorker.class);
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOGGER.info("############# {} started ##############", LocalNsgaIIWorker.class.getSimpleName());
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
						.getTaskForExecution(NsgaII.NSGAII_WORK_QUEUE);

				synchronized (stop) {
					if (stop) {
						LOGGER.info("############# {} Worker stopped ###############", LocalNsgaIIWorker.class.getSimpleName());
						break;
					}
				}

				if (!(task instanceof StopTask)) {
					NsgaIITask nsgaIITask = (NsgaIITask)task;
					double[] fValues = new double[2];
					fValues[0] = Functions.f1(nsgaIITask.getY());
					fValues[1] = Functions.f2(nsgaIITask.getY());
					
					NsgaIIResult result = new NsgaIIResult(nsgaIITask.getId(), nsgaIITask.getSlot(), fValues);
					jobManager.writeResult(NsgaII.NSGAII_RESULT_STORE, result);
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
