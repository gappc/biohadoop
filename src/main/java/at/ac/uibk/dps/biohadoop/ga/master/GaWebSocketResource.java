package at.ac.uibk.dps.biohadoop.ga.master;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class GaWebSocketResource implements ShutdownHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaWebSocketResource.class);

	private JobManager<int[], Double> jobManager = JobManager.getInstance();
	private Task<int[]> currentTask;
	private ObjectMapper om = new ObjectMapper();
	private Session resourceSession;
	private boolean close = false;

	public GaWebSocketResource() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
	}

	@OnOpen
	public void open(Session session) {
		LOGGER.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		resourceSession = session;
	}

	@OnClose
	public void onClose(Session session) throws InterruptedException {
		LOGGER.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		if (currentTask != null) {
			jobManager.reschedule(currentTask, Ga.GA_QUEUE);
		}
		close = true;
	}

	@OnMessage
	public Message<?> onMessage(Message<Double> message, Session session)
			throws InterruptedException {
		LOGGER.debug("WebSocket message for URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		if (message.getType() == MessageType.REGISTRATION_REQUEST) {
			Double[][] distances = DistancesGlobal.getDistancesAsObject();
			Task<Double[][]> task = new Task<Double[][]>(TaskId.newInstance(),
					distances);
			return new Message(MessageType.REGISTRATION_RESPONSE, task);
		}
		if (message.getType() == MessageType.WORK_INIT_REQUEST) {
			currentTask = jobManager.getTask(Ga.GA_QUEUE);
			MessageType messageType = null;
			if (currentTask == null) {
				messageType = MessageType.SHUTDOWN;
			} else {
				messageType = MessageType.WORK_INIT_RESPONSE;
			}
			return new Message<>(messageType, currentTask);
		}
		if (message.getType() == MessageType.WORK_REQUEST) {
			Task<Double> task = om
					.convertValue(message.getPayload(), Task.class);
			jobManager.putResult(task, Ga.GA_QUEUE);
			
			currentTask = jobManager.getTask(Ga.GA_QUEUE);
			
			MessageType messageType = null;
			if (currentTask == null) {
				messageType = MessageType.SHUTDOWN;
			} else {
				messageType = MessageType.WORK_RESPONSE;
			}
			return new Message(messageType, currentTask);
		}

		System.out.println("!!!!!!!!! SHOULD NOT COME HERE !!!!!!!!!!!!!!");
		return new Message(MessageType.NONE, null);
	}

	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		LOGGER.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		jobManager.reschedule(currentTask, Ga.GA_QUEUE);
	}

	@Override
	public void shutdown() {
		if (resourceSession != null && !close) {
			LOGGER.info("WebSocket closing for URI {} and sessionId {}",
					resourceSession.getRequestURI(), resourceSession.getId());
		}
		if (resourceSession == null) {
			LOGGER.info("WebSocket was not opened");
		}
	}
}
