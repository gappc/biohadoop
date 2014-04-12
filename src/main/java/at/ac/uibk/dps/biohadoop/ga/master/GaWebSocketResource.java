package at.ac.uibk.dps.biohadoop.ga.master;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class GaWebSocketResource {

	private static final Logger logger = LoggerFactory
			.getLogger(GaWebSocketResource.class);

	private JobManager jobManager = JobManager.getInstance();
	private Task currentTask;
	private ObjectMapper om = new ObjectMapper();

	@OnOpen
	public void open(Session session) {
		logger.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnMessage
	public Message onMessage(Message message, Session session)
			throws InterruptedException {
		logger.debug("WebSocket message for URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		if (message.getType() == MessageType.REGISTRATION_REQUEST) {
			return new Message(MessageType.REGISTRATION_RESPONSE, null);
		}
		if (message.getType() == MessageType.WORK_INIT_REQUEST) {
			currentTask = (Task) jobManager
					.getTaskForExecution(Ga.GA_WORK_QUEUE);
			return new Message(MessageType.WORK_INIT_RESPONSE, new Object[] {
					DistancesGlobal.getDistances(), currentTask });
		}
		if (message.getType() == MessageType.WORK_REQUEST) {
			GaResult result = om.convertValue(message.getData(), GaResult.class);
			jobManager
					.writeResult(Ga.GA_RESULT_STORE, result);
			currentTask = (Task) jobManager
					.getTaskForExecution(Ga.GA_WORK_QUEUE);
			return new Message(MessageType.WORK_RESPONSE, currentTask);
		}

		throw new RuntimeException(
				"Could not identify Websocket worker request");

		// if (message.getSlot() != -1) {
		// jobManager.writeResult(Ga.GA_RESULT_STORE, message);
		// }
		// currentTask = (Task)
		// jobManager.getTaskForExecution(Ga.GA_WORK_QUEUE);
		// return (GaTask) currentTask;
	}

	/*
	 * Example for Simple message passing, using JSON measured: about 150ms for
	 * 1000 calls from WebSocketGaClient
	 */
	// @OnMessage
	// public GaTask onMessage(GaResult result, Session session)
	// throws InterruptedException {
	// GaTask task = new GaTask();
	// task.setGenome(new int[2]);
	// task.setSlot(0);
	// return task;
	// }

	/*
	 * Example for Simple message passing, using JSON measured: about 80ms -
	 * 100ms for 1000 calls from WebSocketGaClient
	 */
	// @OnMessage
	// public String onMessage(String result, Session session)
	// throws InterruptedException {
	// return res;
	// }

	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		logger.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		jobManager.reScheduleTask(Ga.GA_WORK_QUEUE, currentTask);
		t.printStackTrace();
	}
}
