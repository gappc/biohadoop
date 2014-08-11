package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import at.ac.uibk.dps.biohadoop.queue.TaskFuture;

public class ClassNameTaskFuture<T> implements TaskFuture<T> {

	private final TaskFuture<ClassNameWrapper<T>> wrapped;

	public ClassNameTaskFuture(TaskFuture<T> wrapped) {
		this.wrapped = (TaskFuture<ClassNameWrapper<T>>)wrapped;
	}

	@Override
	public T get() throws InterruptedException {
		return wrapped.get().getWrapped();
	}

	@Override
	public boolean isDone() {
		return wrapped.isDone();
	}
}
