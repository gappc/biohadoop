package at.ac.uibk.dps.biohadoop.queue;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a size-limited storage for results. The results are stored at a
 * given index. After <i>size</i> storage operations, the observer is notified
 * with notifyAll() and the storage is resetted
 * 
 * @author Christian Gapp
 * 
 */
public class ResultStore {
	
	private static final Logger logger = LoggerFactory.getLogger(ResultStore.class);

	private int size = 0;
	private AtomicInteger count = new AtomicInteger();
	private Object[] results;
	private Monitor monitor;

	public ResultStore(int size, Monitor monitor) {
		this.size = size;
		this.monitor = monitor;
		results = new Object[size];
	}

	public Object[] getResults() {
		return results;
	}

	public void store(int index, Object result) {
		results[index] = result;
		count.incrementAndGet();
//		logger.debug("ResultStore size = " + count.intValue());
		if (count.intValue() == size) {
			wakeObserver();
			count.set(0);
		}
	}

	public void reset() {
		count.set(0);
	}

	public int getCount() {
		return count.intValue();
	}

	public void wakeObserver() {
		if (monitor != null) {
			synchronized (monitor) {
//				logger.debug("NOTIFY!!!");
				monitor.setWasSignalled(true);
				monitor.notify();
//				logger.debug("NOTIFIED!!!");
			}
		}
	}
}
