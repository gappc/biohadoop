package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue that can be used to add tasks for asynchronous computation. The tasks
 * can then be consumed by e.g. adapters, which send them to waiting workers.
 * The workers return the results to the adapters, which use the method
 * {@link #storeResult(TaskId, Object)} to store the result. Methods are
 * provided to add tasks to the internal queue, to get them out of the queue, to
 * return the result of an asynchronous computation, to reschedule a task and to
 * stop the queue.
 * 
 * @author Christian Gapp
 *
 */
public class TaskQueue {

	private static final Logger LOG = LoggerFactory.getLogger(TaskQueue.class);

	private final BlockingQueue<Task<?>> queue = new LinkedBlockingQueue<>();
	private final Map<TaskId, TaskQueueEntry> workingSet = new ConcurrentHashMap<>();

	// private final Counter queueSizeCounter = Metrics.getInstance().counter(
	// MetricRegistry.name(TaskQueue.class, this + "-queue-size"));

	/**
	 * Add a task to the task queue to make it available for asynchronous
	 * computation by a worker. This method blocks, if the underlying queue is
	 * full.
	 * 
	 * @param data
	 *            that should be added to the task queue. This data is send to a
	 *            waiting worker for computation
	 * @param TaskConfiguration
	 *            defines the {@link TaskConfiguration} for this task
	 * @return {@link TaskFuture} that represents the result of the asynchronous
	 *         computation
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible
	 */
	public <T> TaskFuture<T> add(Object data,
			TaskConfiguration<?> taskConfiguration) throws InterruptedException {
		TaskId taskId = TaskId.newInstance();
		LOG.debug("Adding task {}", taskId);
		Task<?> task = new Task<>(taskId, taskConfiguration.getTaskTypeId(),
				data);
		TaskFutureImpl<T> taskFutureImpl = new TaskFutureImpl<>();
		TaskQueueEntry taskQueueEntry = new TaskQueueEntry(task,
				taskFutureImpl, taskConfiguration);
		workingSet.put(task.getTaskId(), taskQueueEntry);
		queue.put(task);
		// queueSizeCounter.inc();
		LOG.debug("Task {} was put to queue", task);
		return taskFutureImpl;
	}

	/**
	 * Submit a list of tasks to the task queue, to make them available for
	 * asynchronous computation by workers. This method may throw an exception
	 * at the submission of each element of the list. The tasks of the list,
	 * that were submitted until that point are accepted and will be
	 * distributed, the remaining tasks of the list are not submitted. In the
	 * case of an exception, there is no way to tell which tasks have been
	 * submitted and which not. If no exception is thrown, all tasks have been
	 * submitted. This method blocks, if the underlying queue is full.
	 * 
	 * @param datas
	 *            that should be added to the task queue. This datas are send to
	 *            waiting workers for computation
	 * @param TaskConfiguration
	 *            defines the {@link TaskConfiguration} for this task
	 * @return list of {@link TaskFuture} that represents the results of the
	 *         asynchronous computation. One element is returned for each
	 *         element in the input list.
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible. At the moment
	 *             it is not possible to tell which elements of the list have
	 *             been submitted when the exception occurs.
	 */
	public <T, S> List<TaskFuture<S>> addAll(List<T> datas,
			TaskConfiguration<?> taskConfiguration) throws InterruptedException {
		LOG.debug("Adding list of tasks with size {}", datas.size());
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (T data : datas) {
			TaskFuture<S> taskFutureImpl = add(data, taskConfiguration);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}

	/**
	 * Submit an array of tasks to the task queue, to make them available for
	 * asynchronous computation by workers. This method may throw an exception
	 * at the submission of each element of the array. The tasks of the array,
	 * that were submitted until that point are accepted and will be
	 * distributed, the remaining tasks of the array are not submitted. In the
	 * case of an exception, there is no way to tell which tasks have been
	 * submitted and which not. If no exception is thrown, all tasks have been
	 * submitted. This method blocks, if the underlying queue is full.
	 * 
	 * @param datas
	 *            that should be added to the task queue. This datas are send to
	 *            waiting workers for computation
	 * @param TaskConfiguration
	 *            defines the {@link TaskConfiguration} for this task
	 * @return list of {@link TaskFuture} that represents the results of the
	 *         asynchronous computation. One element is returned for each
	 *         element in the input array.
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible. At the moment
	 *             it is not possible to tell which elements of the array have
	 *             been submitted when the exception occurs.
	 */
	public <T, S> List<TaskFuture<S>> addAll(T[] datas,
			TaskConfiguration<?> taskConfiguration) throws InterruptedException {
		LOG.debug("Adding list of tasks with size {}", datas.length);
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (T data : datas) {
			TaskFuture<S> taskFutureImpl = add(data, taskConfiguration);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}

	/**
	 * Get a task from the underlying queue. This method blocks, if the
	 * underlying queue is empty.
	 * 
	 * @return a {@link Task} from the underlying queue
	 * @throws InterruptedException
	 *             if getting a task from the underlying queue is interrupted
	 */
	public Task<?> getTask() throws InterruptedException {
		return queue.take();
	}

	/**
	 * Get a task from the underlying queue. This method does not block, the
	 * underlying queue is empty, null is returned.
	 * 
	 * @return a {@link Task} from the underlying queue or
	 *         <tt>null<tt> if the queue is empty
	 * @throws InterruptedException
	 *             if getting a task from the underlying queue is interrupted
	 */
	public Task<?> pollTask() throws InterruptedException {
		return queue.poll();
	}

	/**
	 * Gets the {@link TaskConfiguration} that was submitted along with the
	 * task, identified by <tt>taskId</tt>. If the <tt>taskId</tt> is unknown, a
	 * {@link TaskException} is thrown.
	 * 
	 * @param taskId
	 *            is the unique identifier of a task
	 * @return the {@link TaskConfiguration} that was submitted along with the task,
	 *         identified by <tt>taskId</tt>
	 * @throws TaskException
	 *             if the <tt>taskId<tt> is unknown
	 */
	public TaskConfiguration<?> getTaskConfiguration(TaskId taskId)
			throws TaskException {
		if (taskId == null) {
			throw new TaskException("TaskId can not be null");
		}
		TaskQueueEntry entry = workingSet.get(taskId);
		if (entry == null) {
			throw new TaskException("Could not find initial data for task "
					+ taskId);
		}
		return entry.getTaskConfiguration();
	}

	/**
	 * Forwards the result of an asynchronous computation to the corresponding
	 * {@link TaskFuture} by setting its data. This sets the
	 * {@link TaskFuture#isDone()}, unblocks threads that are waiting on the
	 * {@link TaskFuture#get()} method and lets them retrieve the result.
	 * 
	 * @param taskId
	 *            the unique id of the task, for which the result of the
	 *            asynchronous computation is returned
	 * @param data
	 *            is the result of the asynchronous computation
	 * @throws TaskException
	 *             if the taskId is unknown
	 */
	public <S> void storeResult(TaskId taskId, S data) throws TaskException {
		LOG.debug("Putting result for task {}", taskId);
		TaskQueueEntry taskQueueEntry = workingSet.remove(taskId);
		if (taskQueueEntry == null) {
			throw new TaskException("Could not store result for task " + taskId
					+ ", because the task id is not known");
		}
		@SuppressWarnings("unchecked")
		TaskFutureImpl<S> taskFutureImpl = (TaskFutureImpl<S>) taskQueueEntry
				.getTaskFutureImpl();
		taskFutureImpl.set(data);
	}

	/**
	 * Reschedule an already existing task, e.g. if an error occurred. This
	 * method blocks, if the underlying queue is full.
	 * 
	 * @param taskId
	 *            the unique id of the task that should be rescheduled
	 * @throws InterruptedException
	 *             if submitting the task to the underlying queue is interrupted
	 * @throws TaskException
	 *             if the taskId is unknown
	 */
	public void reschedule(TaskId taskId) throws InterruptedException,
			TaskException {
		LOG.info("Rescheduling task {}", taskId);
		TaskQueueEntry taskQueueEntry = workingSet.get(taskId);
		if (taskQueueEntry == null) {
			throw new TaskException("Could not store result for task " + taskId
					+ ", because the task id is not known");
		}
		queue.put(taskQueueEntry.getTask());
		// queueSizeCounter.inc();
	}

}