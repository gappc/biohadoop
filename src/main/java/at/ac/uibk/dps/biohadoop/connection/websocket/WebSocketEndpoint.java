package at.ac.uibk.dps.biohadoop.connection.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class WebSocketEndpoint implements Endpoint, MasterConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketEndpoint.class);

	private final TaskEndpoint<?, ?> taskEndpoint = new TaskEndpointImpl<>(
			Ga.GA_QUEUE);

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
		ShutdownWaitingService.register();
	}

	@OnMessage
	public Message<?> onMessage(Message<Double> message, Session session) {
		if (close) {
			LOG.info("CLOSE");
			return new Message<>(MessageType.SHUTDOWN, null);
		}

		inputMessage = message;

		if (message.getType() == MessageType.REGISTRATION_REQUEST) {
			masterEndpoint.handleRegistration();
		}
		if (message.getType() == MessageType.WORK_INIT_REQUEST) {
			masterEndpoint.handleWorkInit();
		}
		if (message.getType() == MessageType.WORK_REQUEST) {
			masterEndpoint.handleWork();
		}

		return outputMessage;
	}

	@OnClose
	public void onClose(Session session) throws InterruptedException {
		LOG.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		Task<?> currentTask = masterEndpoint.getCurrentTask();
		if (currentTask != null) {
			taskEndpoint.reschedule(currentTask.getTaskId());
		}
		close = true;
		ShutdownWaitingService.unregister();
	}

	@OnError
	public void onError(Session session, Throwable t)
			throws InterruptedException {
		Task<?> currentTask = masterEndpoint.getCurrentTask();
		LOG.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		taskEndpoint.reschedule(currentTask.getTaskId());
		ShutdownWaitingService.unregister();
	}

	@Override
	public void stop() {
		LOG.info("Stopping {}", Helper.getClassname(WebSocketEndpoint.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>) inputMessage;
	}

	@Override
	public <T> void send(Message<T> message) throws SendException {
		outputMessage = message;
	}

}
