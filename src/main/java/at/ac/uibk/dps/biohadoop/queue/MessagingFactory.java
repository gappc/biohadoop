package at.ac.uibk.dps.biohadoop.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import at.ac.uibk.dps.biohadoop.job.Task;

/**
 * @author Christian Gapp
 * 
 */
public class MessagingFactory {

	private static Map<String, BlockingQueue<Task>> workQueues = new HashMap<String, BlockingQueue<Task>>();
	private static Map<String, ResultStore> resultStores = new HashMap<String, ResultStore>();

	private MessagingFactory() {
	}

	public static synchronized BlockingQueue<Task> getWorkQueue(String name) {
		BlockingQueue<Task> queue = workQueues.get(name);
		if (queue == null) {
			queue = new LinkedBlockingQueue<Task>();
			workQueues.put(name, queue);
		}
		return queue;
	}

	/**
	 * Returns a result storage. If it doesn't exist, it is created
	 * 
	 * @param name
	 * @param size
	 * @return
	 */
	public static synchronized ResultStore getResultStore(String name, int size) {
		ResultStore store = resultStores.get(name);
		if (store == null) {
			store = new ResultStore(size);
			resultStores.put(name, store);
		}
		return store;
	}

	/**
	 * Returns a result storage. If it doesn't exist, a RuntimeException is
	 * thrown
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized ResultStore getResultStore(String name) {
		ResultStore store = resultStores.get(name);
		if (store == null) {
			throw new RuntimeException("ResultStore with name " + name
					+ " could not be found");
		}
		return store;
	}
}
