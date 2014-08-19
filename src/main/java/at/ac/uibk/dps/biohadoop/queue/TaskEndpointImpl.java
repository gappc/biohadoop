package at.ac.uibk.dps.biohadoop.queue;

public class TaskEndpointImpl<T, S> implements TaskEndpoint<T, S> {
	
	private final String queueName;
	private final TaskQueue<T, S> taskQueue;
	
	public TaskEndpointImpl(String queueName) {
		this.queueName = queueName;
		this.taskQueue = TaskQueueService.getInstance().<T, S>getTaskQueue(queueName);
	}
	
	@Override
	public Task<T> getTask() throws TaskException, ShutdownException {
		try {
			return taskQueue.getTask();
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while getting task from queue " + queueName);
		}
	}
	
	@Override
	public void storeResult(TaskId taskId, S data) throws TaskException, ShutdownException {
		 try {
			 taskQueue.storeResult(taskId, data);
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while storing task " + taskId + " to queue " + queueName);
		}
	}
	
	@Override
	public void reschedule(TaskId taskId) throws TaskException, ShutdownException {
		try {
			taskQueue.reschedule(taskId);
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while rescheduling task " + taskId + " to queue " + queueName);
		}
	}
}
