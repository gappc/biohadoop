package at.ac.uibk.dps.biohadoop.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskQueue<T, S>  {

	private static final Logger LOG = LoggerFactory.getLogger(TaskQueue.class);
	
	private final BlockingQueue<Task<T>> queue = new LinkedBlockingQueue<>();
	private final Map<TaskId, TaskQueueEntry<T, S>> workingSet = new ConcurrentHashMap<>();
	private final Map<Thread, Thread> waitingThreads = new ConcurrentHashMap<>();
	private final AtomicBoolean stop = new AtomicBoolean(false);
	
	public TaskFuture<S> add(Task<T> task)
			throws InterruptedException {
		LOG.debug("Adding task {}", task);
		TaskFutureImpl<S> taskFutureImpl = new TaskFutureImpl<>();
		TaskQueueEntry<T, S> taskQueueEntry = new TaskQueueEntry<>(task, taskFutureImpl);
		workingSet.put(task.getTaskId(), taskQueueEntry);
		queue.put(task);
		LOG.debug("Task {} was put to queue", task);
		return taskFutureImpl;
	}

	public List<TaskFuture<S>> addAll(List<Task<T>> tasks)
			throws InterruptedException {
		LOG.debug("Adding list of tasks with size {}", tasks.size());
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (Task<T> task : tasks) {
			TaskFuture<S> taskFutureImpl = add(task);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}

	public List<TaskFuture<S>> addAll(Task<T>[] tasks)
			throws InterruptedException {
		LOG.debug("Adding list of tasks with size {}", tasks.length);
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (Task<T> task : tasks) {
			TaskFuture<S> taskFutureImpl = add(task);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}
	
	public Task<T> getTask() throws InterruptedException {
		if (stop.get()) {
			Thread.currentThread().interrupt();
		}
		waitingThreads.put(Thread.currentThread(), Thread.currentThread());
		Task<T> task = queue.take();
		waitingThreads.remove(Thread.currentThread());
		return task;
	}

	public void storeResult(TaskId taskId, S data) throws InterruptedException {
		LOG.debug("Putting result for task {}", taskId);
		TaskQueueEntry<T, S> taskQueueEntry = workingSet.remove(taskId);
		TaskFutureImpl<S> taskFutureImpl = taskQueueEntry.getTaskFutureImpl();
		taskFutureImpl.set(data);
	}
	
	public void reschedule(TaskId taskId) throws InterruptedException {
		LOG.info("Rescheduling task {}", taskId);
		TaskQueueEntry<T, S> taskQueueEntry = workingSet.get(taskId);
		queue.put(taskQueueEntry.getTask());
	}
	
	public void stopQueue() {
		stop.set(true);
		LOG.info("Interrupting all waiting Threads");
		for (Thread thread : waitingThreads.keySet()) {
			LOG.debug("Interrupting {}", thread);
			thread.interrupt();
		}
	}
}