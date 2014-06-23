package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public interface TaskClient<T, S> {

	public TaskFuture<S> add(T taskRequest)
			throws InterruptedException;

	public List<TaskFuture<S>> addAll(List<T> taskRequests)
			throws InterruptedException;

}
