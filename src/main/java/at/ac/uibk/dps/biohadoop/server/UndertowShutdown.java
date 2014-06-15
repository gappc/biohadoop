package at.ac.uibk.dps.biohadoop.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class UndertowShutdown {

	private final static AtomicInteger count = new AtomicInteger();
	private static CountDownLatch latch = new CountDownLatch(0);
	
	public synchronized static void increaseShutdownCount() {
		count.incrementAndGet();
		latch = new CountDownLatch(count.get());
	}
	
	public synchronized static void decreaseLatch() {
		latch.countDown();
	}
	
	public synchronized static CountDownLatch getLatch() {
		return latch;
	}
}
