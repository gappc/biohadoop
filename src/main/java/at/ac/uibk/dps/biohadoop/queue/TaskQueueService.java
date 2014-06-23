package at.ac.uibk.dps.biohadoop.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskQueueService {

	private final static Logger LOG = LoggerFactory.getLogger(TaskQueueService.class);
	private final static TaskQueueService TASK_QUEUE_MANAGER = new TaskQueueService();
	
	private final Map<String, TaskQueue<?, ?>> queues = new ConcurrentHashMap<>();
	private final Object monitor = new Object();
	
	private TaskQueueService() {
	}
	
	public static TaskQueueService getInstance() {
		return TASK_QUEUE_MANAGER;
	}
	
	public TaskQueue<?, ?> getTaskQueue(String name) {
		LOG.debug("Getting queue with name {}", name);
		TaskQueue<?, ?> queue = queues.get(name);
		if (queue == null) {
			synchronized (monitor) {
				queue = queues.get(name);
				if (queue == null) {
					LOG.info("Instanciated new queue with name {}", name);
					queue = new TaskQueue<Object, Object>();
					queues.put(name, queue);
				}
			}
		}
		return queue;
	}
}
