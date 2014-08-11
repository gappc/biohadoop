package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public class DedicatedTaskClient<R, T, S> implements TaskClient<T, S> {

	private final TaskQueue<T, S> taskQueue;

	public DedicatedTaskClient(String queueName) {
		taskQueue = getTaskQueue(queueName);
	}

	@Override
	public TaskFuture<S> add(T taskRequest)
			throws InterruptedException {
//		return taskQueue.add(taskRequest);
		return null;
	}

	@Override
	public List<TaskFuture<S>> addAll(List<T> taskRequests)
			throws InterruptedException {
//		return taskQueue.addAll(taskRequests);
		return null;
	}
	
	@Override
	public List<TaskFuture<S>> addAll(T[] taskRequests)
			throws InterruptedException {
//		return taskQueue.addAll(taskRequests);
		return null;
	}

	@SuppressWarnings("unchecked")
	private TaskQueue<T, S> getTaskQueue(String queueName) {
		return (TaskQueue<T, S>) TaskQueueService.getInstance().getTaskQueue(
				queueName);
	}

}
