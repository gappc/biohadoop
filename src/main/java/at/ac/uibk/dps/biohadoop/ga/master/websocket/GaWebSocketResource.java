package at.ac.uibk.dps.biohadoop.ga.master.websocket;

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
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class GaWebSocketResource implements Endpoint, ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaWebSocketResource.class);

	private JobManager<int[], Double> jobManager = JobManager.getInstance();
//	private ObjectMapper om = new ObjectMapper();
	private Session resourceSession;
	private boolean close = false;
	
	private Master<int[]> master = new GaMasterImpl<int[]>(this);
	private Message<?> inputMessage;
	private Message<?> outputMessage;
	
	public GaWebSocketResource() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
	}

	@OnOpen
	public void open(Session session) {
		LOG.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		resourceSession = session;
	}

	@OnMessage
	public Message<?> onMessage(Message<Double> message, Session session) {
		inputMessage = message;
		
		try {
			if (message.getType() == MessageType.REGISTRATION_REQUEST) {
				master.handleRegistration();
			}
			if (message.getType() == MessageType.WORK_INIT_REQUEST) {
				master.handleWorkInit();
			}
			if (message.getType() == MessageType.WORK_REQUEST) {
				master.handleWork();
			}
			return outputMessage;
		} catch (ShutdownException e) {
			LOG.info("Got shutdown event");
			return outputMessage;
		}
	}

	@OnClose
	public void onClose(Session session) throws InterruptedException {
		LOG.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		Task<int[]> currentTask = master.getCurrentTask();
		if (currentTask != null) {
			jobManager.reschedule(currentTask, Ga.GA_QUEUE);
		}
		close = true;
	}
	
	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		Task<int[]> currentTask = master.getCurrentTask();
		LOG.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		jobManager.reschedule(currentTask, Ga.GA_QUEUE);
	}

	@Override
	public void shutdown() {
		if (resourceSession != null && !close) {
			LOG.info("WebSocket closing for URI {} and sessionId {}",
					resourceSession.getRequestURI(), resourceSession.getId());
		}
		if (resourceSession == null) {
			LOG.info("WebSocket was not opened");
		}
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>)inputMessage;
	}

	@Override
	public void send(Message<?> message) throws SendException {
		outputMessage = message;
	}
}
