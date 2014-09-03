package at.ac.uibk.dps.biohadoop.queue;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;

/**
 * This class provides a base implementation of {@link TaskSubmitter}, with
 * methods to add tasks to the Task system
 * 
 * @author Christian Gapp
 *
 * @param <R>
 * @param <T>
 * @param <S>
 */
public class SimpleTaskSubmitter<R, T, S> implements TaskSubmitter<T, S> {

	public static final String QUEUE_NAME = "DEFAULT_QUEUE";

	private final TaskQueue<R, T, S> taskQueue;
	private final String remoteExecutableClassName;
	private final R initialData;

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the default task queue "UNIFIED_QUEUE". The class defined by
	 * <tt>communicationClass</tt> is used when computing the result.
	 * 
	 * @param communicationClass
	 *            defines the class that is used to compute the result of a task
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass) {
		this(communicationClass, QUEUE_NAME, null);
	}

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the default task queue "UNIFIED_QUEUE". The class defined by
	 * <tt>communicationClass</tt> is used when computing the result. The
	 * <tt>initialData</tt> is send to the Worker Endpoints when they first
	 * encounter the <tt>communicationClass</tt> type of work.
	 * 
	 * @param communicationClass
	 *            defines the class that is used to compute the result of a task
	 * @param initialData
	 *            is send to the Worker Endpoints when they first encounter the
	 *            <tt>communicationClass</tt> type of work.
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass,
			R initialData) {
		this(communicationClass, QUEUE_NAME, initialData);
	}

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the task queue defined by <tt>queueName</tt>. If the
	 * <tt>queueName</tt> differs from the default queue name "UNIFIED_QUEUE",
	 * it is considered a dedicated queue. Its jobs can be handled by dedicated
	 * Master and Worker Endpoints only. If you would like to use the shared
	 * queue, consider using {@link #SimpleTaskSubmitter(Class)}. The class
	 * defined by <tt>communicationClass</tt> is used when computing the result.
	 * 
	 * @param remoteExecutableClass
	 *            defines the class that is used to compute the result of a task
	 * @param queueName
	 * @param initialData
	 *            is send to the Worker Endpoints when they first encounter the
	 *            <tt>communicationClass</tt> type of work.
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String queueName, R initialData) {
		taskQueue = TaskQueueService.getInstance().<R, T, S> getTaskQueue(
				queueName);
		this.remoteExecutableClassName = remoteExecutableClass
				.getCanonicalName();
		// TODO copy object to prevent user from (accidentially) changing the
		// initialData after SimpleTaskSubmitter is constructed
		this.initialData = initialData;
	}

	public TaskFuture<S> add(T data) throws TaskException {
		return submitTask(data);
	}

	public List<TaskFuture<S>> addAll(List<T> datas) throws TaskException {
		List<TaskFuture<S>> taskFutures = new ArrayList<TaskFuture<S>>();
		for (T data : datas) {
			TaskFuture<S> taskFuture = submitTask(data);
			taskFutures.add(taskFuture);
		}
		return taskFutures;
	}

	public List<TaskFuture<S>> addAll(T[] datas) throws TaskException {
		List<TaskFuture<S>> taskFutures = new ArrayList<TaskFuture<S>>();
		for (T data : datas) {
			TaskFuture<S> taskFuture = submitTask(data);
			taskFutures.add(taskFuture);
		}
		return taskFutures;
	}

	private TaskFuture<S> submitTask(T data) throws TaskException {
		try {
			return taskQueue.add(data, remoteExecutableClassName, initialData);
		} catch (InterruptedException e) {
			throw new TaskException("Could not add Task", e);
		}
	}

}
