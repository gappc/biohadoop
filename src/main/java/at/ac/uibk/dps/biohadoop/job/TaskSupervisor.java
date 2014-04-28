package at.ac.uibk.dps.biohadoop.job;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSupervisor implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TaskSupervisor.class);

	private int sleep = 1000;
	private Boolean stop = false;

	public TaskSupervisor() {
		super();
	}

	public TaskSupervisor(int sleep) {
		super();
		this.sleep = sleep;
	}

	@Override
	public void run() {
		JobManager jobManager = JobManager.getInstance();
		jobManager.addObserver(this);
		try {
			while (true) {
				synchronized (stop) {
					if (stop) {
						LOGGER.info("Stopping task supervisor");
						break;
					}
				}

				int count = 0;
				Long now = System.currentTimeMillis();
				Map<Long, Job> tasks = jobManager.getTasks();
				for (Long l : tasks.keySet()) {
					Job job = tasks.get(l);
					if (job != null && now - job.getCreated() > sleep) {
						LOGGER.error("Job {} hanging at state {}", job
								.getTask(), job.getTaskState());
						count++;
					}
				}

				LOGGER.info("Hanging jobs: " + count);
				Thread.sleep(sleep);
			}
		} catch (Exception e) {
			LOGGER.error("Error while scanning for hanging tasks", e);
		}
	}

	@Override
	public void stop() {
		synchronized (stop) {
			stop = true;
		}
	}

}
