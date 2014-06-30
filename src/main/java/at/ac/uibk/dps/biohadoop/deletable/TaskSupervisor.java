package at.ac.uibk.dps.biohadoop.deletable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSupervisor implements Runnable {

	private static final Logger LOG = LoggerFactory
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
		try {
			while (true) {
				synchronized (stop) {
					if (stop) {
						LOG.info("Stopping task supervisor");
						break;
					}
				}

				int count = 0;
//				Long now = System.currentTimeMillis();
//				Map<Long, Job> tasks = jobService.getTasks();
//				for (Long l : tasks.keySet()) {
//					Job job = tasks.get(l);
//					if (job != null && now - job.getCreated() > sleep) {
//						LOGGER.error("Job {} hanging at state {}", job
//								.getTask(), job.getTaskState());
//						count++;
//					}
//				}

				LOG.info("Hanging jobs: " + count);
				Thread.sleep(sleep);
			}
		} catch (Exception e) {
			LOG.error("Error while scanning for hanging tasks", e);
		}
	}

}
