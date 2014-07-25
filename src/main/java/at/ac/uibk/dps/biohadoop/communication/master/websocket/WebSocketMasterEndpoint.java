package at.ac.uibk.dps.biohadoop.communication.master.websocket;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.communication.master.ReceiveException;
import at.ac.uibk.dps.biohadoop.communication.master.SendException;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.rest.ResourcePath;
import at.ac.uibk.dps.biohadoop.communication.master.rest.RestMaster;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

@ServerEndpoint(value = "/{path}", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketMasterEndpoint implements MasterSendReceive, MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketMasterEndpoint.class);

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
	public void open(@PathParam("path") String path, Session session) {
		LOG.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		ShutdownWaitingService.register();
		
		session.getRequestURI();
		try {
			Class<? extends Master> superComputable = ResourcePath
					.getWebSocketEntry(path);
			String queueName = superComputable.getAnnotation(RestMaster.class)
					.queueName();
			taskEndpoint = new TaskEndpointImpl<>(queueName);
			
			buildMasterEndpoint(path);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnMessage
	public Message<?> onMessage(@PathParam("path") String path,
			Message<Double> message, Session session) {
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
				session.close(new CloseReason(
						CloseReason.CloseCodes.UNEXPECTED_CONDITION, errMsg));
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
		LOG.info("Stopping {}",
				ClassnameProvider.getClassname(WebSocketMasterEndpoint.class));
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
	
	private void buildMasterEndpoint(String path) throws InstantiationException, IllegalAccessException {
		Class<? extends Master> superComputable = ResourcePath
				.getWebSocketEntry(path);
		String queueName = superComputable.getAnnotation(RestMaster.class)
				.queueName();
		Object registrationObject = superComputable.newInstance()
				.getRegistrationObject();
		masterEndpoint = DefaultMasterImpl.newInstance(this, queueName,
				registrationObject);
	}

}
