package at.ac.uibk.dps.biohadoop.queue;

public interface TaskEndpoint<T, S> {

	public Task<T> getTask() throws InterruptedException;

	public void storeResult(TaskId taskId, S data)
			throws InterruptedException;
	
	public void reschedule(TaskId taskId)
			throws InterruptedException;
}
