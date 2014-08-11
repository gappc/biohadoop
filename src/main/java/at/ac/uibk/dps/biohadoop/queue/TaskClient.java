package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public interface TaskClient<T, S> {

	public TaskFuture<S> add(T data)
			throws InterruptedException;

	public List<TaskFuture<S>> addAll(List<T> datas)
			throws InterruptedException;
	
	public List<TaskFuture<S>> addAll(T[] datas)
			throws InterruptedException;
	
}
