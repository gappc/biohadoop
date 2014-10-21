package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	private static final Map<String, TaskQueue<?, ?, ?>> queues = new ConcurrentHashMap<>();
	private static final Object monitor = new Object();

	private TaskQueueService() {
		// Nothing to do
	}

	/**
	 * Returns a task queue for the given name. If no such task queue exists, a
	 * new one is created
	 * 
	 * @param name
	 * @return
	 */
	public static <R, T, S> TaskQueue<R, T, S> getTaskQueue(String name) {
		LOG.debug("Getting queue with name {}", name);
		TaskQueue<R, T, S> queue = (TaskQueue<R, T, S>) queues.get(name);
		if (queue == null) {
			synchronized (monitor) {
				queue = (TaskQueue<R, T, S>) queues.get(name);
				if (queue == null) {
					queue = (TaskQueue<R, T, S>) new TaskQueue<Object, Object, Object>();
					LOG.info("Instanciated new queue with name {}", name);
					queues.put(name, queue);
				}
			}
		}
		return queue;
	}

}
