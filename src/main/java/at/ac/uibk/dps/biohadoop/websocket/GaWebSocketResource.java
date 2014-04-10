package at.ac.uibk.dps.biohadoop.websocket;

import java.util.concurrent.BlockingQueue;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.ga.Ga;
import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.queue.MessagingFactory;
import at.ac.uibk.dps.biohadoop.queue.ResultStore;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketGaResultDecoder.class)
public class GaWebSocketResource {
	
	private BlockingQueue<Object> workQueue = MessagingFactory.getWorkQueue(Ga.GA_WORK_QUEUE);
	private ResultStore resultStore = MessagingFactory.getResultStore(Ga.GA_RESULT_STORE);

	@OnOpen
	public void open(Session session) {
		System.out.println("onOpen");
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("onClose");
	}

	@OnMessage
	public GaTask onMessage(GaResult result, Session session)
			throws InterruptedException {
		if (result.getSlot() != -1) {
			resultStore.store(result.getSlot(), result.getResult());
		}
		return (GaTask)workQueue.take();
	}
	

	/*
	 * Example for Simple message passing, using JSON
	 * measured: about 150ms for 1000 calls from WebSocketGaClient
	 */
//	@OnMessage
//	public GaTask onMessage(GaResult result, Session session)
//			throws InterruptedException {
//		GaTask task = new GaTask();
//		task.setGenome(new int[2]);
//		task.setSlot(0);
//		return task;
//	}
	
	/*
	 * Example for Simple message passing, using JSON
	 * measured: about 80ms - 100ms for 1000 calls from WebSocketGaClient
	 */
//	@OnMessage
//	public String onMessage(String result, Session session)
//			throws InterruptedException {
//		return res;
//	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("onerror");
		t.printStackTrace();
	}
}
