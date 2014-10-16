package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.metrics.Metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

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
 * @param <T>
 *            Type for the task data i.e. type of data that is used in
 *            asynchronous computation
 * @param <S>
 *            Type of the result of an asynchronous computation
 */
public class TaskQueue<R, T, S> {

	private static final Logger LOG = LoggerFactory.getLogger(TaskQueue.class);

	private final BlockingQueue<Task<T>> queue = new LinkedBlockingQueue<>();
	private final Map<TaskId, TaskQueueEntry<R, T, S>> workingSet = new ConcurrentHashMap<>();
	private final Map<Thread, Thread> waitingThreads = new ConcurrentHashMap<>();
	private final Counter queueSizeCounter = Metrics.getInstance().counter(
			MetricRegistry.name(TaskQueue.class, this + "-queue-size"));

	/**
	 * Add a task to the task queue to make it available for asynchronous
	 * computation by a worker. This method blocks, if the underlying queue is
	 * full.
	 * 
	 * @param data
	 *            that should be added to the task queue. This data is send to a
	 *            waiting worker for computation
	 * @param asyncComputableClassName
	 *            defines the class that should be used by a worker to compute
	 *            the result for this data
	 * @param initialData
	 *            is send to a worker the first time it encounters this
	 *            asyncComputableClassName
	 * @return {@link TaskFuture} that represents the result of the asynchronous
	 *         computation
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible
	 */
	public TaskFuture<S> add(T data, TaskConfiguration<R> taskConfiguration)
			throws InterruptedException {
		TaskId taskId = TaskId.newInstance();
		LOG.debug("Adding task {}", taskId);
		Task<T> task = new Task<>(taskId, taskConfiguration.getTaskTypeId(),
				data);
		TaskFutureImpl<S> taskFutureImpl = new TaskFutureImpl<>();
		TaskQueueEntry<R, T, S> taskQueueEntry = new TaskQueueEntry<>(task,
				taskFutureImpl, taskConfiguration);
		workingSet.put(task.getTaskId(), taskQueueEntry);
		queue.put(task);
		queueSizeCounter.inc();
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
	 * @param asyncComputableClassName
	 *            defines the class that should be used by a worker to compute
	 *            the result for this data
	 * @param initialData
	 *            is send to a worker the first time it encounters this
	 *            asyncComputableClassName
	 * @return list of {@link TaskFuture} that represents the results of the
	 *         asynchronous computation. Exactly one element is returned for
	 *         each element in the input list.
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible. At the moment
	 *             it is not possible to tell which elements of the list have
	 *             been submitted when the exception occurs.
	 */
	public List<TaskFuture<S>> addAll(List<T> datas,
			TaskConfiguration<R> taskConfiguration) throws InterruptedException {
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
	 * @param asyncComputableClassName
	 *            defines the class that should be used by a worker to compute
	 *            the result for this data
	 * @param initialData
	 *            is send to a worker the first time it encounters this
	 *            asyncComputableClassName
	 * @return list of {@link TaskFuture} that represents the results of the
	 *         asynchronous computation. Exactly one element is returned for
	 *         each element in the input array.
	 * @throws InterruptedException
	 *             if adding data to the queue was not possible. At the moment
	 *             it is not possible to tell which elements of the array have
	 *             been submitted when the exception occurs.
	 */
	public List<TaskFuture<S>> addAll(T[] datas,
			TaskConfiguration<R> taskConfiguration) throws InterruptedException {
		LOG.debug("Adding list of tasks with size {}", datas.length);
		List<TaskFuture<S>> taskFutures = new ArrayList<>();
		for (T data : datas) {
			TaskFuture<S> taskFutureImpl = add(data, taskConfiguration);
			taskFutures.add(taskFutureImpl);
		}
		return taskFutures;
	}

	/**
	 * Get a task from the underlying queue. If the queue was advised to
	 * shutdown by Biohadoop, an invocation of this method interrupts the
	 * calling thread by calling <tt>Thread.currentThread().interrupt()</tt>.
	 * This method blocks, if the underlying queue is empty.
	 * 
	 * @return a {@link Task} from the underlying queue
	 * @throws InterruptedException
	 *             if getting a task from the underlying queue is interrupted
	 */
	public Task<T> getTask() throws InterruptedException {
		waitingThreads.put(Thread.currentThread(), Thread.currentThread());
		Task<T> task = queue.take();
		waitingThreads.remove(Thread.currentThread());
		queueSizeCounter.dec();
		return task;
	}

	/**
	 * Gets the <tt>initialData</tt> that was submitted along with the task,
	 * identified by <tt>taskId</tt>. If the <tt>taskId</tt> is unknown, a
	 * {@link TaskException} is thrown.
	 * 
	 * @param taskId
	 *            is the unique identifier of a task
	 * @return the <tt>initialData</tt> that was submitted along with the task,
	 *         identified by <tt>taskId</tt>
	 * @throws TaskException
	 *             if the <tt>taskId<tt> is unknown
	 */
	public TaskConfiguration<R> getTaskConfiguration(TaskId taskId) throws TaskException {
		if (taskId == null) {
			throw new TaskException("TaskId can not be null");
		}
		TaskQueueEntry<R, T, S> entry = workingSet.get(taskId);
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
	public void storeResult(TaskId taskId, S data) throws TaskException {
		LOG.debug("Putting result for task {}", taskId);
		TaskQueueEntry<R, T, S> taskQueueEntry = workingSet.remove(taskId);
		if (taskQueueEntry == null) {
			throw new TaskException("Could not store result for task " + taskId
					+ ", because the task id is not known");
		}
		TaskFutureImpl<S> taskFutureImpl = taskQueueEntry.getTaskFutureImpl();
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
		TaskQueueEntry<R, T, S> taskQueueEntry = workingSet.get(taskId);
		if (taskQueueEntry == null) {
			throw new TaskException("Could not store result for task " + taskId
					+ ", because the task id is not known");
		}
		queue.put(taskQueueEntry.getTask());
		queueSizeCounter.inc();
	}

	/**
	 * Stops this queue by interrupting all threads that are blocked at the
	 * underlying queue. Methods that may block are {@link #add(Task)},
	 * {@link #addAll(List)}, {@link #addAll(Task[])}, {@link #getTask()},
	 * {@link #reschedule(TaskId)}, {@link #storeResult(TaskId, Object)}
	 */
	public void stopQueue() {
		LOG.info("Interrupting all waiting Threads");
		forceShutdown();
	}

	/**
	 * Starts a daemon thread that goes into an infinite loop to interrupt all
	 * waiting threads. This is necessary, as we keep no worker state and it is
	 * possible, that a worker blocks on the {@link #getTask()} method, which
	 * would prevent the system from shutting down.
	 */
	private void forceShutdown() {
		Thread shutdownForcer = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					for (Thread thread : waitingThreads.keySet()) {
						LOG.debug("Interrupting {}", thread);
						thread.interrupt();
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						LOG.error(
								"Got interrupted while waiting for next round of forceful shutdown",
								e);
					}
				}
			}
		}, "TaskQueue-ShutdownForcer");
		shutdownForcer.setDaemon(true);
		shutdownForcer.start();
	}

}