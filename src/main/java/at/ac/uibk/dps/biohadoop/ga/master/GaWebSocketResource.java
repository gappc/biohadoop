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
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class GaWebSocketResource implements WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaWebSocketResource.class);

	private JobManager jobManager = JobManager.getInstance();
	private Task currentTask;
	private ObjectMapper om = new ObjectMapper();
	private Session resourceSession;

	public GaWebSocketResource() {
		jobManager.addObserver(this);
	}

	@OnOpen
	public void open(Session session) {
		LOGGER.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		resourceSession = session;
	}

	@OnClose
	public void onClose(Session session) {
		LOGGER.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnMessage
	public Message onMessage(Message message, Session session)
			throws InterruptedException {
		LOGGER.debug("WebSocket message for URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		MessageType messageType = MessageType.NONE;
		Object response = null;

		if (message.getType() == MessageType.REGISTRATION_REQUEST) {
			messageType = MessageType.REGISTRATION_RESPONSE;
			response = null;
		}
		if (message.getType() == MessageType.WORK_INIT_REQUEST) {
			currentTask = (Task) jobManager
					.getTaskForExecution(Ga.GA_WORK_QUEUE);
			messageType = MessageType.WORK_INIT_RESPONSE;
			response = new Object[] { DistancesGlobal.getDistances(),
					currentTask };
		}
		if (message.getType() == MessageType.WORK_REQUEST) {
			GaResult result = om
					.convertValue(message.getData(), GaResult.class);
			jobManager.writeResult(Ga.GA_RESULT_STORE, result);
			currentTask = (Task) jobManager
					.getTaskForExecution(Ga.GA_WORK_QUEUE);

			if (currentTask instanceof StopTask) {
				messageType = MessageType.SHUTDOWN;
			} else {
				messageType = MessageType.WORK_RESPONSE;
			}
			response = currentTask;
		}

		return new Message(messageType, response);
	}

	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		LOGGER.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		jobManager.reScheduleTask(Ga.GA_WORK_QUEUE, currentTask);
	}

	@Override
	public void stop() {
		if (resourceSession != null) {
			LOGGER.info("WebSocket closing for URI {} and sessionId {}",
					resourceSession.getRequestURI(), resourceSession.getId());
		} else {
			LOGGER.info("WebSocket was not opened");
		}
	}
}
