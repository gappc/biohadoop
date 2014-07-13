package at.ac.uibk.dps.biohadoop.connection.websocket;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.connection.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.CommunicationException;
import at.ac.uibk.dps.biohadoop.endpoint.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.torename.Helper;

public abstract class WebSocketEndpoint implements MasterSendReceive, MasterLifecycle,
		MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketEndpoint.class);

	private TaskEndpoint<?, ?> taskEndpoint;

	private DefaultMasterImpl masterEndpoint;
	private Message<?> inputMessage;
	private Message<?> outputMessage;
	private boolean close = false;

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
		buildMasterEndpoint();
		taskEndpoint = new TaskEndpointImpl<>(getQueueName());
	}

	@OnMessage
	public Message<?> onMessage(Message<Double> message, Session session) {
		if (close) {
			LOG.info("CLOSE");
			return new Message<>(MessageType.SHUTDOWN, null);
		}

		inputMessage = message;
		try {
			if (message.getType() == MessageType.REGISTRATION_REQUEST) {
				masterEndpoint.handleRegistration();
			}
			if (message.getType() == MessageType.WORK_INIT_REQUEST) {
				masterEndpoint.handleWorkInit();
			}
			if (message.getType() == MessageType.WORK_REQUEST) {
				masterEndpoint.handleWork();
			}
		} catch (CommunicationException e) {
			String errMsg = "Error while communicating with worker, closing communication";
			LOG.error(errMsg, e);
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, errMsg));
			} catch (IOException e1) {
				LOG.error("Could not close connection", e1);
			}
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

	private void buildMasterEndpoint() {
		masterEndpoint = DefaultMasterImpl.newInstance(this, getQueueName(),
				getRegistrationObject());
	}

}
