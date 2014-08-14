package at.ac.uibk.dps.biohadoop.queue;


public interface TaskFuture<T> {

	public T get() throws TaskException;
	public boolean isDone();
	
}
