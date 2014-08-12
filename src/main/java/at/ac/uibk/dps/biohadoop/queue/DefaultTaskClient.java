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

	public TaskFuture<S> add(T data) throws InterruptedException {
		Task<T> task = new ClassNameWrappedTask<>(TaskId.newInstance(), data,
				remoteExecutableclassName);
		return taskQueue.add(task);
	}

	public List<TaskFuture<S>> addAll(List<T> datas)
			throws InterruptedException {
		List<TaskFuture<S>> taskFutures = new ArrayList<TaskFuture<S>>();
		for (T data : datas) {
			Task<T> task = new ClassNameWrappedTask<>(TaskId.newInstance(),
					data, remoteExecutableclassName);
			TaskFuture<S> taskFuture = taskQueue.add(task);
			taskFutures.add(taskFuture);
		}
		return taskFutures;
	}

	public List<TaskFuture<S>> addAll(T[] datas) throws InterruptedException {
		List<TaskFuture<S>> taskFutures = new ArrayList<TaskFuture<S>>();
		for (T data : datas) {
			Task<T> task = new ClassNameWrappedTask<>(TaskId.newInstance(),
					data, remoteExecutableclassName);
			TaskFuture<S> taskFuture = taskQueue.add(task);
			taskFutures.add(taskFuture);
		}
		return taskFutures;
	}

}
