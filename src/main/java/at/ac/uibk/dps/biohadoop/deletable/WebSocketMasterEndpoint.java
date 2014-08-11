package at.ac.uibk.dps.biohadoop.deletable;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;
@Deprecated
//@ServerEndpoint(value = "/{path}", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketMasterEndpoint implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketMasterEndpoint.class);

	private TaskEndpoint<?, ?> taskEndpoint;

	private DefaultMasterImpl masterEndpoint;
	private boolean close = false;

	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> master) {
		Annotation annotation = master.getAnnotation(DedicatedWebSocket.class);
		ResourcePath.addWebSocketEntry(((DedicatedWebSocket) annotation).queueName(),
				master);
		DeployingClasses.addWebSocketClass(WebSocketMasterEndpoint.class);
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
			Message<?> inputMessage, Session session) {
		if (close) {
			LOG.info("CLOSE");
			return new Message<>(MessageType.SHUTDOWN, null);
		}

//		try {
//			if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
//				Object registrationObject = getRegistrationObject(path);
//				return masterEndpoint.handleRegistration(registrationObject);
//			}
//			if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
//				return masterEndpoint.handleWorkInit();
//			}
//			if (inputMessage.getType() == MessageType.WORK_REQUEST) {
//				return masterEndpoint.handleWork(inputMessage);
//			}
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// Something went wrong, try to close connection
		try {
			String errMsg = "Error while communicating with worker, closing communication";
			session.close(new CloseReason(
					CloseReason.CloseCodes.UNEXPECTED_CONDITION, errMsg));
		} catch (IOException e1) {
			LOG.error("Could not close connection", e1);
		}
		return null;
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

	private void buildMasterEndpoint(String path)
			throws InstantiationException, IllegalAccessException {
//		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = ResourcePath
//				.getWebSocketEntry(path);
//		String queueName = masterClass.getAnnotation(DedicatedWebSocket.class)
//				.queueName();
//		masterEndpoint = DefaultMasterImpl.newInstance(queueName);
//		taskEndpoint = new TaskEndpointImpl<>(queueName);
	}
	
	private Object getRegistrationObject(String path) throws InstantiationException, IllegalAccessException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = ResourcePath
				.getWebSocketEntry(path);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}

}
