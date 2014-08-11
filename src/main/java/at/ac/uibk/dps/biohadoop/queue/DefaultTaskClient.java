package at.ac.uibk.dps.biohadoop.queue;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameTaskFuture;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;

public class DefaultTaskClient<R, T, S> implements TaskClient<T, S> {

	public static final String QUEUE_NAME = "UNIFIED_QUEUE";

	private final TaskQueue<ClassNameWrapper<T>, S> taskQueue;
	private final String communicationClass;

	public DefaultTaskClient(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass) {
		this(communicationClass, QUEUE_NAME);
	}
	
	public DefaultTaskClient(
			Class<? extends RemoteExecutable<R, T, S>> communicationClass, String  queueName) {
		taskQueue = (TaskQueue<ClassNameWrapper<T>, S>) TaskQueueService
				.getInstance().getTaskQueue(queueName);
		this.communicationClass = communicationClass.getCanonicalName();
	}

	@Override
	public TaskFuture<S> add(T taskRequest) throws InterruptedException {
		ClassNameWrapper<T> wrappedTaskRequest = new ClassNameWrapper<>(communicationClass, taskRequest);
		return new ClassNameTaskFuture<S>(taskQueue.add(wrappedTaskRequest));
	}

	@Override
	public List<TaskFuture<S>> addAll(List<T> taskRequests)
			throws InterruptedException {
		List<ClassNameWrapper<T>> wrappedTaskRequests = new ArrayList<>();
		for (T taskRequest : taskRequests) {
			ClassNameWrapper<T> wrappedTaskRequest = new ClassNameWrapper<T>(communicationClass,
					taskRequest);
			wrappedTaskRequests.add(wrappedTaskRequest);
		}
		List<TaskFuture<S>> taskFutures = taskQueue.addAll(wrappedTaskRequests);
		for (TaskFuture<S> taskFuture : taskFutures) {
			taskFuture = new ClassNameTaskFuture<S>(taskFuture);
		}
		return taskFutures;
	}

	@Override
	public List<TaskFuture<S>> addAll(T[] taskRequests)
			throws InterruptedException {
		ClassNameWrapper<T>[] wrappedTaskRequests = new ClassNameWrapper[taskRequests.length];
		for (int i = 0; i < taskRequests.length; i++) {
			ClassNameWrapper<T> wrappedTaskRequest = new ClassNameWrapper<T>(communicationClass,
					taskRequests[i]);
			wrappedTaskRequests[i] = wrappedTaskRequest;
		}
		List<TaskFuture<S>> taskFutures = taskQueue.addAll(wrappedTaskRequests);
		for (TaskFuture<S> taskFuture : taskFutures) {
			taskFuture = new ClassNameTaskFuture<S>(taskFuture);
		}
		return taskFutures;
	}

}
