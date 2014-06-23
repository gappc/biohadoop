package at.ac.uibk.dps.biohadoop.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskQueue<T, S> implements TaskClient<T, S>, TaskEndpoint<T, S> {

	private final static Logger LOG = LoggerFactory.getLogger(TaskQueue.class);
	
	private final BlockingQueue<Task<T>> queue = new LinkedBlockingQueue<>();
	private final Map<TaskId, TaskQueueEntry<T, S>> workingSet = new ConcurrentHashMap<>();

	@Override
	public TaskFuture<S> add(T taskRequest)
			throws InterruptedException {
		LOG.debug("Adding task request {}", taskRequest);
		Task<T> task = new Task<T>(TaskId.newInstance(), taskRequest);
		TaskFutureImpl<S> taskFutureImpl = new TaskFutureImpl<>();
		
		TaskQueueEntry<T, S> taskQueueEntry = new TaskQueueEntry<>(task, taskFutureImpl);
		workingSet.put(task.getTaskId(), taskQueueEntry);
		queue.put(task);
		LOG.debug("Task request {} was put to queue", taskRequest);
		return taskFutureImpl;
	}

	@Override
	public List<TaskFuture<S>> addAll(List<T> taskRequests)
			throws InterruptedException {
		LOG.debug("Adding list of task requests with size {}", taskRequests.size());
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (T taskRequest : taskRequests) {
			TaskFuture<S> taskFutureImpl = add(taskRequest);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}

	@Override
	public Task<T> getTask() throws InterruptedException {
		return queue.take();
	}

	@Override
	public void putResult(TaskId taskId, S data) throws InterruptedException {
		LOG.debug("Putting result for task {}", taskId);
		TaskQueueEntry<T, S> taskQueueEntry = workingSet.remove(taskId);
		TaskFutureImpl<S> taskFutureImpl = taskQueueEntry.getTaskFutureImpl();
		taskFutureImpl.set(data);
	}
	
	@Override
	public void reschedule(TaskId taskId) throws InterruptedException {
		LOG.info("Rescheduling task {}", taskId);
		TaskQueueEntry<T, S> taskQueueEntry = workingSet.get(taskId);
		queue.put(taskQueueEntry.getTask());
	}
	
}