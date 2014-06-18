package at.ac.uibk.dps.biohadoop.solver.nsgaii.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.job.Task;
import at.ac.uibk.dps.biohadoop.service.job.api.JobService;
import at.ac.uibk.dps.biohadoop.service.solver.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class LocalNsgaIIWorker implements Runnable, ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalNsgaIIWorker.class);

	private final String className = Helper
			.getClassname(LocalNsgaIIWorker.class);

	private Boolean stop = false;
	private int logSteps = 1000;

	@Override
	public void run() {
		LOG.info("############# {} started ##############",
				LocalNsgaIIWorker.class.getSimpleName());
		JobService<double[], double[]> jobService = JobService.getInstance();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			performanceLogger.step(LOG);
			try {
				Task<double[]> task = jobService.getTask(NsgaII.NSGAII_QUEUE);
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
				jobService.putResult(result, NsgaII.NSGAII_QUEUE);
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
