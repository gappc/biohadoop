package at.ac.uibk.dps.biohadoop.queue;

import java.util.concurrent.CountDownLatch;


public class TaskFutureImpl<T> implements TaskFuture<T> {

	private T data;
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Override
	public T get() throws InterruptedException {
		latch.await();
		return data;
	}
	
	public void set(T data) {
		this.data = data;
		latch.countDown();
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}
}
