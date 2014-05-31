package at.ac.uibk.dps.biohadoop.torename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;

public class TaskSupervisor implements Runnable, ShutdownHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TaskSupervisor.class);

	private int sleep = 2000;
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
		ApplicationManager.getInstance().registerShutdownHandler(this);
		try {
			while (true) {
				synchronized (stop) {
					if (stop) {
						LOGGER.info("Stopping task supervisor");
						break;
					}
				}

				int count = 0;
//				Long now = System.currentTimeMillis();
//				Map<Long, Job> tasks = jobManager.getTasks();
//				for (Long l : tasks.keySet()) {
//					Job job = tasks.get(l);
//					if (job != null && now - job.getCreated() > sleep) {
//						LOGGER.error("Job {} hanging at state {}", job
//								.getTask(), job.getTaskState());
//						count++;
//					}
//				}

				LOGGER.info("Hanging jobs: " + count);
				Thread.sleep(sleep);
			}
		} catch (Exception e) {
			LOGGER.error("Error while scanning for hanging tasks", e);
		}
	}

	@Override
	public void shutdown() {
		synchronized (stop) {
			stop = true;
		}
	}

}
