package at.ac.uibk.dps.biohadoop.performance.test.master.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.performance.test.JobRequest;
import at.ac.uibk.dps.biohadoop.performance.test.JobResponse;
import at.ac.uibk.dps.biohadoop.performance.test.config.PerformanceLauncher;

public class PerformanceSocketResource implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PerformanceSocketResource.class);
	private String className = PerformanceSocketResource.class.getName();

	private Socket socket;
	private JobManager jobManager = JobManager.getInstance();
	private int logSteps = 10000;

	public PerformanceSocketResource(Socket socket) {
		this.socket = socket;
	}

	@Override
//	TODO use try-with-resource
	public void run() {
		jobManager.addObserver(this);
		try {
			LOGGER.info("{} opened Socket on server", className);

			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(socket.getOutputStream()));
			os.flush();
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(socket.getInputStream()));

			JobRequest request = new JobRequest();
			long startTime = System.currentTimeMillis();
			int counter = 0;
			while (counter < PerformanceLauncher.MAX_ITERATIONS) {
				if (counter % logSteps == 0) {
					long endTime = System.currentTimeMillis();
					LOGGER.info("Counter: {} | {}ms for last {} computations, got {} from worker",
							counter, endTime - startTime, logSteps, request.getJob());
					startTime = System.currentTimeMillis();
					os.reset();
				}
				counter++;

				request = (JobRequest)is.readObject();
				
				JobResponse response = new JobResponse(JobResponse.State.RUNJOB, (long) counter);
				os.writeObject(response);
				os.flush();
			}
			is.readObject();
			os.writeObject(new JobResponse(JobResponse.State.DIE, (long)counter));
			os.flush();
			os.close();
			is.close();
			
			jobManager.stopAllWorkers();
		} catch (Exception e) {
			LOGGER.error("Error while running {} socket server", className, e);
		}
	}

	@Override
	public void stop() {
		LOGGER.info("{} socket server shutting down", className);
	}
}
