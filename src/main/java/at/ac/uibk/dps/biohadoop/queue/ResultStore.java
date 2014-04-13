package at.ac.uibk.dps.biohadoop.queue;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.Task;

/**
 * Provides a size-limited storage for results. The results are stored at a
 * given index. After <i>size</i> storage operations, the observer is notified
 * with notifyAll() and the storage is resetted
 * 
 * @author Christian Gapp
 * 
 */
public class ResultStore {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResultStore.class);

	private int size = 0;
	private AtomicInteger count = new AtomicInteger();
	private Task[] results;
	private Monitor monitor = new Monitor();

	public ResultStore(int size) {
		this.size = size;
		results = new Task[size];
	}

	public Task[] getResults() {
		synchronized (results) {
			return results;
		}
	}

	public synchronized void store(int index, Task result) {
		synchronized (results) {
			results[index] = result;
			count.incrementAndGet();
			LOGGER.debug("ResultStore size = " + count.intValue());
			if (count.intValue() == size) {
				wakeObserver();
				count.set(0);
			}
		}
	}

	public void reset() {
		count.set(0);
	}

	public int getCount() {
		return count.intValue();
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public void wakeObserver() {
		if (monitor != null) {
			synchronized (monitor) {
				LOGGER.debug("NOTIFY!!!");
				monitor.setWasSignalled(true);
				monitor.notifyAll();
				LOGGER.debug("NOTIFIED!!!");
			}
		}
	}
}
