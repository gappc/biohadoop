package at.ac.uibk.dps.biohadoop.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Christian Gapp
 * 
 */
public class MessagingFactory {

	private static Map<String, BlockingQueue<Object>> workQueues = new HashMap<String, BlockingQueue<Object>>();
	private static Map<String, ResultStore> resultStores = new HashMap<String, ResultStore>();

	public static BlockingQueue<Object> getWorkQueue(String name) {
		BlockingQueue<Object> queue = workQueues.get(name);
		if (queue == null) {
			queue = new LinkedBlockingQueue<Object>();
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
	public static ResultStore getResultStore(String name, int size,
			Monitor monitor) {
		ResultStore store = resultStores.get(name);
		if (store == null) {
			store = new ResultStore(size, monitor);
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
	public static ResultStore getResultStore(String name) {
		ResultStore store = resultStores.get(name);
		if (store == null) {
			throw new RuntimeException("ResultStore with name " + name + " could not be found");
		}
		return store;
	}
}
