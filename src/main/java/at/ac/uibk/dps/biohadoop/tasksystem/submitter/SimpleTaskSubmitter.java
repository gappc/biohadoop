package at.ac.uibk.dps.biohadoop.tasksystem.submitter;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.tasksystem.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskFuture;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;

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

	public static final String PIPELINE_NAME = "DEFAULT_PIPELINE";

	private final TaskQueue<R, T, S> taskQueue;
	private final String remoteExecutableClassName;
	private final R initialData;

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the default task pipeline {@value #PIPELINE_NAME} with a queue named
	 * {@value #PIPELINE_NAME}. The class defined by <tt>communicationClass</tt>
	 * is used when computing the result on a worker.
	 * 
	 * @param communicationClass
	 *            defines the class that is used to compute the result of a task
	 *            on a worker
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass) {
		this(communicationClass, PIPELINE_NAME, null);
	}

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the default task pipeline {@value #PIPELINE_NAME} with a queue named
	 * {@value #PIPELINE_NAME}. The class defined by <tt>communicationClass</tt>
	 * is used when computing the result on a worker. The <tt>initialData</tt>
	 * is send to a worker when it first encounters the
	 * <tt>communicationClass</tt> type of work.
	 * 
	 * @param communicationClass
	 *            defines the class that is used to compute the result of a task
	 *            on a worker
	 * @param initialData
	 *            is send to a worker when it first encounters the
	 *            <tt>communicationClass</tt> type of work.
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass,
			R initialData) {
		this(communicationClass, PIPELINE_NAME, initialData);
	}

	/**
	 * Creates a <tt>SimpleTaskSubmitter</tt>, that is capable of adding tasks
	 * to the task pipeline, that is identified by <tt>pipelineName</tt>. If the
	 * <tt>pipelineName</tt> differs from the default task pipeline name
	 * {@value #PIPELINE_NAME}, it is considered a dedicated pipeline. Its jobs
	 * can only be handled by adapters and workers, that are also part of this
	 * pipeline. If you would like to use the default pipeline, consider using
	 * {@link #SimpleTaskSubmitter(Class)}. The class defined by
	 * <tt>communicationClass</tt> is used when computing the result of a task
	 * on a worker.
	 * 
	 * @param remoteExecutableClass
	 *            defines the class that is used to compute the result of a task
	 *            on a worker
	 * @param pipelineName
	 *            defines the name of the dedicated pipeline
	 * @param initialData
	 *            is send to a worker when it first encounters the
	 *            <tt>communicationClass</tt> type of work.
	 */
	public SimpleTaskSubmitter(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String pipelineName, R initialData) {
		taskQueue = TaskQueueService.getInstance().<R, T, S> getTaskQueue(
				pipelineName);
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
