package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public class TaskClientImpl<T, S> implements TaskClient<T, S> {

	private final String queueName;

	public TaskClientImpl(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public TaskFuture<S> add(T taskRequest)
			throws InterruptedException {
		return getTaskQueue().add(taskRequest);
	}

	@Override
	public List<TaskFuture<S>> addAll(List<T> taskRequests)
			throws InterruptedException {
		return getTaskQueue().addAll(taskRequests);
	}

	@SuppressWarnings("unchecked")
	private TaskQueue<T, S> getTaskQueue() {
		return (TaskQueue<T, S>) TaskQueueService.getInstance().getTaskQueue(
				queueName);
	}

}
