package at.ac.uibk.dps.biohadoop.moead.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class LocalMoeadWorker implements Runnable, ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalMoeadWorker.class);

	private final String className = Helper
			.getClassname(LocalMoeadWorker.class);
	
	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOG.info("############# {} started ##############", className);
		JobManager<double[], double[]> jobManager = JobManager.getInstance();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			performanceLogger.step(LOG);
			try {
				Task<double[]> task = jobManager.getTask(Moead.MOEAD_QUEUE);
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
				jobManager.putResult(result, Moead.MOEAD_QUEUE);
				Thread.sleep(0);
			} catch (InterruptedException e) {
				LOG.error("Error while running {}", className, e);
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
