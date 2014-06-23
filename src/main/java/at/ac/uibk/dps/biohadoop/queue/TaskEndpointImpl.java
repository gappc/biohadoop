package at.ac.uibk.dps.biohadoop.queue;

public class TaskEndpointImpl<T, S> implements TaskEndpoint<T, S> {
	
	private final String queueName;
	
	public TaskEndpointImpl(String queueName) {
		this.queueName = queueName;
	}
	
	@Override
	public Task<T> getTask() throws InterruptedException {
		return getTaskQueue().getTask();
	}
	
	@Override
	public void putResult(TaskId taskId, S data) throws InterruptedException {
		 getTaskQueue().putResult(taskId, data);
	}
	
	@Override
	public void reschedule(TaskId taskId) throws InterruptedException {
		getTaskQueue().reschedule(taskId);
	}
	
	@SuppressWarnings("unchecked")
	private TaskQueue<T, S> getTaskQueue() {
		return (TaskQueue<T, S>)TaskQueueService.getInstance().getTaskQueue(queueName);
	}
}
