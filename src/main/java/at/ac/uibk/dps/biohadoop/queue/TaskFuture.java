package at.ac.uibk.dps.biohadoop.queue;


public interface TaskFuture<T> {

	public T get() throws InterruptedException;
	public boolean isDone();
	
}
