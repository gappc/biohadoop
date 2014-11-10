package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;

/**
 * This class provides methods to add tasks to the task system
 * 
 * @author Christian Gapp
 *
 * @param <R>
 * @param <T>
 * @param <S>
 */
public class TaskSubmitter<R, T, S> {

	private final TaskQueue taskQueue = TaskQueueService.getTaskQueue();
	private final TaskConfiguration<R> taskConfiguration;

	/**
	 * Creates a {@link TaskSubmitter}, that can be used to submit Tasks to the
	 * task system. The class defined by <tt>asyncComputableClass</tt> is used
	 * when computing the result on a worker.
	 * 
	 * @param asyncComputableClass
	 *            defines the class that is used to compute the result of a task
	 *            on a worker
	 */
	public TaskSubmitter(
			Class<? extends AsyncComputable<R, T, S>> asyncComputableClass) {
		this(asyncComputableClass, null);
	}

	/**
	 * Creates a {@link TaskSubmitter}, that can be used to submit Tasks to the
	 * task system. The class defined by <tt>asyncComputableClass</tt> is used
	 * when computing the result on a worker. The <tt>initialData</tt> is send
	 * to a worker when it first encounters the <tt>asyncComputableClass</tt>
	 * type of work.
	 * 
	 * @param asyncComputableClass
	 *            defines the class that is used to compute the result of a task
	 *            on a worker
	 * @param initialData
	 *            is send to a worker when it first encounters the
	 *            <tt>asyncComputableClass</tt> type of work.
	 */
	public TaskSubmitter(
			Class<? extends AsyncComputable<R, T, S>> asyncComputableClass,
			R initialData) {
		String asyncComputableClassName = asyncComputableClass
				.getCanonicalName();
		// TODO copy initialData to prevent user from (accidentially) changing
		// the initialData after TaskSubmitter is constructed
		taskConfiguration = new TaskConfiguration<>(asyncComputableClassName,
				initialData);
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
			return taskQueue.add(data, taskConfiguration);
		} catch (InterruptedException e) {
			throw new TaskException("Could not add Task", e);
		}
	}

}
