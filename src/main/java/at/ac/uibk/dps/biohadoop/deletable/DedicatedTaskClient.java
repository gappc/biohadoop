package at.ac.uibk.dps.biohadoop.deletable;

import java.util.List;

import at.ac.uibk.dps.biohadoop.queue.TaskClient;
import at.ac.uibk.dps.biohadoop.queue.TaskFuture;
import at.ac.uibk.dps.biohadoop.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.queue.TaskQueueService;

@Deprecated
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
