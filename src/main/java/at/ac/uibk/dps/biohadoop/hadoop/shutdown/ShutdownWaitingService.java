package at.ac.uibk.dps.biohadoop.hadoop.shutdown;

import java.util.concurrent.atomic.AtomicBoolean;

import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

public class ShutdownWaitingService {

	private static final ZeroLock ZERO_LOCK = new ZeroLock();
	private static final AtomicBoolean IS_FINISHED = new AtomicBoolean(false);

	private ShutdownWaitingService() {
	}
	
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
		IS_FINISHED.set(true);
	}
	
	public static boolean isFinished() {
		return IS_FINISHED.get();
	}
}
