package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import at.ac.uibk.dps.biohadoop.queue.TaskFuture;

public class ClassNameTaskFuture2<T> implements TaskFuture<T> {

	private final TaskFuture<ClassNameWrapper2<T>> wrapped;

	public ClassNameTaskFuture2(TaskFuture<T> wrapped) {
		this.wrapped = (TaskFuture<ClassNameWrapper2<T>>)wrapped;
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
