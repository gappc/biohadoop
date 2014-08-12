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

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.HandleMessageException;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

@ServerEndpoint(value = "/{path}", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class DefaultWebSocketMaster<R, T, S> implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultWebSocketMaster.class);

	private Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass;
	private TaskEndpoint<?, ?> taskEndpoint;
	private DefaultMasterImpl<R, T, S> masterEndpoint;
	private boolean close = false;

	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass) {
		this.remoteExecutableClass = remoteExecutableClass;
		DeployingClasses.addWebSocketClass(DefaultWebSocketMaster.class);
	}

	@Override
	public void start() throws MasterException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	@OnOpen
	public void open(@PathParam("path") String path, Session session) {
		LOG.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		ShutdownWaitingService.register();

		session.getRequestURI();
		try {
			buildMasterEndpoint(null, path);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnMessage
	public Message<T> onMessage(@PathParam("path") String path,
			Message<S> inputMessage, Session session) {
		if (close) {
			LOG.info("CLOSE");
			return new Message<>(MessageType.SHUTDOWN, null);
		}

		try {
			return masterEndpoint.handleMessage(inputMessage);
		} catch (HandleMessageException e) {
			String errMsg = "Could not handle worker request";
			LOG.error(errMsg, e);
			try {
				session.close(new CloseReason(
						CloseReason.CloseCodes.UNEXPECTED_CONDITION, errMsg));
			} catch (IOException e1) {
				LOG.error("Could not close connection to {}", session);
			}
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

	private void buildMasterEndpoint(Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass, String path)
			throws InstantiationException, IllegalAccessException {
		masterEndpoint = new DefaultMasterImpl<>(path);
		taskEndpoint = new TaskEndpointImpl<>(path);
	}

	private Object getRegistrationObject(String className)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
				.forName(className);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}

}
