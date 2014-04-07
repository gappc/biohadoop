package at.ac.uibk.dps.biohadoop.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class SimpleQueue {

	private static LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	
	public static void put(Object data) throws InterruptedException {
		queue.put(data);
	}
	
	public static Object take() throws InterruptedException {
		return queue.take();
	}
}
