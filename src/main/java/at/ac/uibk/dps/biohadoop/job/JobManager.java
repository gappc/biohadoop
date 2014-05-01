package at.ac.uibk.dps.biohadoop.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.queue.MessagingFactory;
import at.ac.uibk.dps.biohadoop.queue.Monitor;
import at.ac.uibk.dps.biohadoop.queue.ResultStore;

public class JobManager {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JobManager.class);

	private Monitor jobMonitor = new Monitor();
	private Random rand = new Random();
	
	private static int storeSize = 20;

	private static JobManager manager = new JobManager();
	private static Map<String, BlockingQueue<Task>> queues = new ConcurrentHashMap<String, BlockingQueue<Task>>();
	private static Map<Long, Job> tasks = new ConcurrentHashMap<Long, Job>();
	private static Map<String, ResultStore> resultStores = new ConcurrentHashMap<String, ResultStore>();
	private static List<WorkObserver> workObservers = new ArrayList<WorkObserver>();

	private static AtomicBoolean stop = new AtomicBoolean(false);
	private static float completed = 0.0f;
	
	private JobManager() {
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

		if (((ConcurrentHashMap<Long, Job>) tasks).putIfAbsent(task.getId(),
				job) != null) {
			LOGGER.error("PUTTING FUCK");
		}

		getQueue(queueName).put(task);
		LOGGER.debug("Task {} {}", task.getId(), TaskState.NEW);
	}

	public void reScheduleTask(String queueName, Task task)
			throws InterruptedException {
		Job job = tasks.get(task.getId());
		TaskState state = job.getTaskState();
		if (state == TaskState.RUNNING) {
			LOGGER.info("Rescheduling task: {}", task);
			job.setTaskState(TaskState.NEW);
			getQueue(queueName).put(task);
		} else {
			LOGGER.error(
					"Rescheduling of task {} not possible, because it is state {}",
					task, state);
		}
	}

	public Task getTaskForExecution(String queueName)
			throws InterruptedException {
		Task task = getQueue(queueName).take();

		Job job = tasks.get(task.getId());
		job.setTaskState(TaskState.RUNNING);

		LOGGER.debug("Task {} {}", task.getId(), TaskState.RUNNING);
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
		LOGGER.debug("Task {} {}", result.getId(), TaskState.FINISHED);
	}

	public Task[] readResult(String resultStoreName) {
		synchronized (jobMonitor) {
			ResultStore resultStore = getResultStore(resultStoreName);
			for (Task t : resultStore.getResults()) {
				tasks.remove(t.getId());
			}
			return resultStore.getResults();
		}
	}

	public Monitor getResultStoreMonitor(String resultStoreName) {
		ResultStore resultStore = getResultStore(resultStoreName);
		return resultStore.getMonitor();
	}

	public void addObserver(WorkObserver observer) {
		LOGGER.info("Registering WorkObserver {}", observer);
		boolean observerRegistered = workObservers.contains(observer);
		if (!observerRegistered) {
			workObservers.add(observer);
			LOGGER.info("WorkObserver {} registered", observer);
		} else {
			LOGGER.warn("WorkObserver {} was already registered", observer);
		}
	}

	public boolean removeObserver(WorkObserver observer) {
		LOGGER.info("Removing WorkObserver {}", observer);
		boolean observerRegistered = workObservers.remove(observer);
		if (observerRegistered) {
			LOGGER.info("WorkObserver {} unregistered", observer);
		} else {
			LOGGER.warn("WorkObserver {} was not registered", observer);
		}
		return observerRegistered;
	}

	public void stopAllWorkers() throws InterruptedException {
		stop.set(true);
		for (WorkObserver observer : workObservers) {
			for (String queueName : queues.keySet()) {
				scheduleTask(queueName, new StopTask());
			}
			observer.stop();
		}
	}
	
	public boolean isStop() {
		return stop.get();
	}

	public Map<Long, Job> getTasks() {
		return Collections.unmodifiableMap(tasks);
	}

	public float getCompleted() {
		return completed;
	}

	public void setCompleted(float completed) {
		JobManager.completed = completed;
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
