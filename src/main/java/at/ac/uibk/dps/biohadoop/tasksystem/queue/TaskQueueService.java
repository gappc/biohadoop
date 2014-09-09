package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines methods to get a named task queue and to stop all task queues. It is
 * strongly suggested to use {@link #getTaskQueue(String)} if a task queue is
 * needed inside Biohadoop.
 * 
 * @author Christian Gapp
 *
 */
public class TaskQueueService {

	private static final Logger LOG = LoggerFactory
			.getLogger(TaskQueueService.class);
	private static final TaskQueueService TASK_QUEUE_MANAGER = new TaskQueueService();

	private final Map<String, TaskQueue<?, ?, ?>> queues = new ConcurrentHashMap<>();
	private final AtomicBoolean isFinished = new AtomicBoolean(false);
	private final Object monitor = new Object();

	private TaskQueueService() {
		// Nothing to do
	}

	/**
	 * Returns an instance of the {@link TaskQueueService}
	 * 
	 * @return
	 */
	public static TaskQueueService getInstance() {
		return TASK_QUEUE_MANAGER;
	}

	/**
	 * Returns a task queue for the given name. If no such task queue exists, a
	 * new one is created
	 * 
	 * @param name
	 * @return
	 */
	public <R, T, S> TaskQueue<R, T, S> getTaskQueue(String name) {
		LOG.debug("Getting queue with name {}", name);
		TaskQueue<R, T, S> queue = (TaskQueue<R, T, S>) queues.get(name);
		if (queue == null) {
			synchronized (monitor) {
				queue = (TaskQueue<R, T, S>) queues.get(name);
				if (queue == null) {
					if (isFinished.get()) {
						LOG.error("Could not instanciate new queue with name {}, because got already the signal to stop all queues");
					} else {
						queue = (TaskQueue<R, T, S>) new TaskQueue<Object, Object, Object>();
						LOG.info("Instanciated new queue with name {}", name);
						queues.put(name, queue);
					}
				}
			}
		}
		return queue;
	}

	/**
	 * Stops all task queues, that are allocated by
	 * {@link #getTaskQueue(String)}, calling their
	 * {@link TaskQueue#stopQueue()} method.
	 */
	public void stopAllTaskQueues() {
		LOG.debug("Stopping all queues");
		synchronized (monitor) {
			isFinished.set(true);
			for (String queueName : queues.keySet()) {
				LOG.info("Stopping queue {}", queueName);
				queues.get(queueName).stopQueue();
				queues.remove(queueName);
			}
		}
	}
}
