package at.ac.uibk.dps.biohadoop.torename;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ZeroLock {

	private final AtomicInteger count = new AtomicInteger();
	private final Lock lock = new ReentrantLock();
	private final Condition allReturned = lock.newCondition();

	public void increment() {
		lock.lock();
		try {
			count.incrementAndGet();
		} finally {
			lock.unlock();
		}
	}

	public void decrement() {
		lock.lock();
		try {
			count.decrementAndGet();
			allReturned.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public void release() {
		lock.lock();
		try {
			count.set(0);
			allReturned.signal();
		} finally {
			lock.unlock();
		}
	}

	public void await() throws InterruptedException {
		lock.lock();
		try {
			while (!count.compareAndSet(0, 0)) {
				allReturned.await();
			}
		} finally {
			lock.unlock();
		}
	}
}
