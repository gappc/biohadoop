package at.ac.uibk.dps.biohadoop.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.queue.MessagingFactory;
import at.ac.uibk.dps.biohadoop.queue.Monitor;
import at.ac.uibk.dps.biohadoop.queue.ResultStore;

public class JobManager {

	private Monitor jobMonitor = new Monitor();
	private List<String> errors = new ArrayList<String>();
	private Random rand = new Random();

	private static final Logger logger = LoggerFactory
			.getLogger(JobManager.class);

	private static int storeSize = 20;

	private static JobManager manager = new JobManager();
	private static Map<String, BlockingQueue<Task>> queues = new ConcurrentHashMap<String, BlockingQueue<Task>>();
	private static Map<Long, Job> tasks = new ConcurrentHashMap<Long, Job>();
	private static Map<String, ResultStore> resultStores = new ConcurrentHashMap<String, ResultStore>();

	private JobManager() {
		checkFuckedUp();
	}

	public static JobManager getInstance() {
		return manager;
	}

	public static JobManager getInstance(int newStoreSize) {
		JobManager.storeSize = newStoreSize;
		return manager;
	}

	/**
	 * Sets the size of new allocated stores
	 * 
	 * @param newStoreSize
	 */
	public void setNewStoreSize(int newStoreSize) {
		storeSize = newStoreSize;
	}

	public void scheduleTask(String queueName, Task task)
			throws InterruptedException {
		task.setId(rand.nextLong());
		Job job = new Job(task, TaskState.NEW);
		
		if (((ConcurrentHashMap<Long, Job>)tasks).putIfAbsent(task.getId(), job) != null) {
			synchronized (errors) {
				errors.add("PUTTING FUCK");
			}
		}

		getQueue(queueName).put(task);
		logger.debug("Task {} {}", task.getId(), TaskState.NEW);
	}
	
	public void reScheduleTask(String queueName, Task task) throws InterruptedException {
		Job job = tasks.get(task.getId());
		TaskState state = job.getTaskState();
		if (state == TaskState.RUNNING) {
			logger.info("Rescheduling task: {}", task);
			job.setTaskState(TaskState.NEW);
			getQueue(queueName).put(task);
		}
		else {
			logger.error("Rescheduling of task {} not possible, because it is state {}", task, state);
		}
	}

	public Task getTaskForExecution(String queueName) throws InterruptedException {
		Task task = getQueue(queueName).take();

		Job job = tasks.get(task.getId());
		job.setTaskState(TaskState.RUNNING);

		logger.debug("Task {} {}", task.getId(), TaskState.RUNNING);
		return task;
	}

	public void writeResult(String resultStoreName, Task result) {
		synchronized (jobMonitor) {
			ResultStore resultStore = getResultStore(resultStoreName);
			if (result instanceof Slotted) {
				resultStore.store(((Slotted) result).getSlot(), result);
			} else {
				resultStore.store(0, result);
			}

			Job job = tasks.get(result.getId());
			job.setTaskState(TaskState.FINISHED);
		}
		logger.debug("Task {} {}", result.getId(), TaskState.FINISHED);
	}

	public Task[] readResult(String resultStoreName) {
		synchronized (jobMonitor) {
			ResultStore resultStore = getResultStore(resultStoreName);
//			logger.info("Stored {}", tasks.size());
			for (Task t : resultStore.getResults()) {
				tasks.remove(t.getId());
			}
			return resultStore.getResults();
		}
	}

	public void checkFuckedUp() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					int sleep = 1000;
					while (true) {
						int count = 0;
						Long now = System.currentTimeMillis();
						for (Long l : tasks.keySet()) {
							Job job = tasks.get(l);
							if (job != null) {
								if (now - job.getCreated() > sleep) {
									logger.error("Job {} fucked up!!! {}", job
											.getTask().getId(), job
											.getTaskState());
									count++;
								}
							}
						}

						try {
							System.out.println("FUCKUP COUNT: " + count);
							synchronized (errors) {
								for (String s : errors) {
									System.out.println(s);
								}
							}
							System.out
									.println("-------------------------------");
							Thread.sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					synchronized (errors) {
						errors.add("#######'FUCKUP Thread - fuck you");
					}
				}
			}
		}).start();
	}

	public Monitor getResultStoreMonitor(String resultStoreName) {
		ResultStore resultStore = getResultStore(resultStoreName);
		return resultStore.getMonitor();
	}

	private BlockingQueue<Task> getQueue(String queueName) {
		BlockingQueue<Task> queue = queues.get(queueName);
		if (queue == null) {
			queue = MessagingFactory.getWorkQueue(queueName);
			queues.put(queueName, queue);
		}
		return queue;
	}

	private ResultStore getResultStore(String resultStoreName) {
		ResultStore resultStore = resultStores.get(resultStoreName);
		if (resultStore == null) {
			resultStore = MessagingFactory.getResultStore(resultStoreName,
					storeSize);
			resultStores.put(resultStoreName, resultStore);
		}
		return resultStore;
	}

}
