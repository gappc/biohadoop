package at.ac.uibk.dps.biohadoop.hadoop.shutdown;

import java.util.concurrent.atomic.AtomicBoolean;

import at.ac.uibk.dps.biohadoop.torename.ZeroLock;

public class ShutdownWaitingService {

	private static final ZeroLock ZERO_LOCK = new ZeroLock();
	private static final AtomicBoolean isFinished = new AtomicBoolean(false);

	public static void register() {
		ZERO_LOCK.increment();
	}

	public static void unregister() {
		ZERO_LOCK.decrement();
	}

	public static void await() throws InterruptedException {
		ZERO_LOCK.await();
	}
	
	public static void setFinished() {
		isFinished.set(true);
	}
	
	public static boolean isFinished() {
		return isFinished.get();
	}
}
