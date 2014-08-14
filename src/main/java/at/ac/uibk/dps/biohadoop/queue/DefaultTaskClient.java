package at.ac.uibk.dps.biohadoop.queue;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;

public class DefaultTaskClient<R, T, S> implements TaskClient<T, S> {

	public static final String QUEUE_NAME = "UNIFIED_QUEUE";

	private final TaskQueue<T, S> taskQueue;
	private final String remoteExecutableclassName;

	public DefaultTaskClient(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass) {
		this(communicationClass, QUEUE_NAME);
	}

	public DefaultTaskClient(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String queueName) {
		taskQueue = (TaskQueue<T, S>) TaskQueueService.getInstance()
				.getTaskQueue(queueName);
		this.remoteExecutableclassName = remoteExecutableClass
				.getCanonicalName();
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
		TaskId taskId = TaskId.newInstance();
		Task<T> task = new ClassNameWrappedTask<>(taskId, data,
				remoteExecutableclassName);
		try {
			return taskQueue.add(task);
		} catch (InterruptedException e) {
			throw new TaskException("Could not add Task " + taskId
					+ " for data " + data, e);
		}
	}

}
