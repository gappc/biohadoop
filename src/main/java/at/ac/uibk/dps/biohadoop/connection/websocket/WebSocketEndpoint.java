package at.ac.uibk.dps.biohadoop.connection.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.server.UndertowShutdown;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class WebSocketEndpoint implements Endpoint, ShutdownHandler, MasterConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketEndpoint.class);

	private final JobManager<int[], Double> jobManager = JobManager.getInstance();
	
	private Session resourceSession;
	private MasterConfiguration masterConfiguration;
	private Message<?> inputMessage;
	private Message<?> outputMessage;
	private boolean close = false;

	protected MasterEndpoint masterEndpoint;
	
	@Override
	public void configure() {
		DeployingClasses.addWebSocketClass(this.getClass());
	}

	@Override
	public void start() {
		
	}

	@OnOpen
	public void open(Session session) {
		LOG.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		
		UndertowShutdown.increaseShutdownCount();
		ApplicationManager.getInstance().registerShutdownHandler(this);
		resourceSession = session;
	}

	@OnMessage
	public Message<?> onMessage(Message<Double> message, Session session) {
		if (close) {
			LOG.info("CLOSE");
			return null;
		}
		inputMessage = message;
		
//		try {
			if (message.getType() == MessageType.REGISTRATION_REQUEST) {
				masterEndpoint.handleRegistration();
			}
			if (message.getType() == MessageType.WORK_INIT_REQUEST) {
				masterEndpoint.handleWorkInit();
			}
			if (message.getType() == MessageType.WORK_REQUEST) {
				masterEndpoint.handleWork();
			}
//		} catch (ShutdownException e) {
//			LOG.info("Got shutdown event");
//		}
		return outputMessage;
	}

	@OnClose
	public void onClose(Session session) throws InterruptedException {
		LOG.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		Task currentTask = masterEndpoint.getCurrentTask();
		if (currentTask != null) {
			jobManager.reschedule(currentTask, masterConfiguration.getQueueName());
		}
		close = true;
		UndertowShutdown.decreaseLatch();
	}
	
	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		Task currentTask = masterEndpoint.getCurrentTask();
		LOG.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		jobManager.reschedule(currentTask, masterConfiguration.getQueueName());
	}

	@Override
	public void shutdown() {
//		if (resourceSession != null && !close) {
//			LOG.info("WebSocket closing for URI {} and sessionId {}",
//					resourceSession.getRequestURI(), resourceSession.getId());
//		}
//		if (resourceSession == null) {
//			LOG.info("WebSocket was not opened");
//		}
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
