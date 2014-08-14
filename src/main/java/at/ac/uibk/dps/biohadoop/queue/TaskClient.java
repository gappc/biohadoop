package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public interface TaskClient<T, S> {

	public TaskFuture<S> add(T data)
			throws TaskException;

	public List<TaskFuture<S>> addAll(List<T> datas)
			throws TaskException;
	
	public List<TaskFuture<S>> addAll(T[] datas)
			throws TaskException;
	
}
