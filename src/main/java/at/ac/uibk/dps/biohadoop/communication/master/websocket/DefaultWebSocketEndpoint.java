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
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.HandleMessageException;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskException;
import at.ac.uibk.dps.biohadoop.webserver.handler.DeployingClasses;

@ServerEndpoint(value = "/{path}", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class DefaultWebSocketEndpoint<R, T, S> implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultWebSocketEndpoint.class);

	private TaskEndpoint<?, ?> taskEndpoint;
	private DefaultMasterImpl<R, T, S> masterEndpoint;
	private boolean close = false;

	@Override
	public void configure(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass) {
		DeployingClasses.addWebSocketClass(DefaultWebSocketEndpoint.class);
	}

	@Override
	public void start() throws MasterException {
		// Nothing to do
	}

	@Override
	public void stop() {
		// Nothing to do
	}

	@OnOpen
	public void open(@PathParam("path") String path, Session session) {
		LOG.info("Opened Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		// if (ShutdownWaitingService.isFinished()) {
		// String errMsg =
		// "All computations finished, new connections not accepted";
		// try {
		// LOG.info(errMsg);
		// session.close(new CloseReason(
		// CloseReason.CloseCodes.NORMAL_CLOSURE, errMsg));
		// } catch (IOException e) {
		// LOG.error("Error while closing session ({})", errMsg, e);
		// }
		// }
		ShutdownWaitingService.register();

		session.getRequestURI();
		buildMasterEndpoint(path);
	}

	@OnMessage
	public Message<T> onMessage(@PathParam("path") String path,
			Message<S> inputMessage, Session session) {
		if (close || ShutdownWaitingService.isFinished()) {
			String errMsg = "All computations finished, new connections not accepted";
			try {
				LOG.info(errMsg);
				session.close(new CloseReason(
						CloseReason.CloseCodes.NORMAL_CLOSURE, errMsg));
			} catch (IOException e) {
				LOG.error("Error while closing session ({})", errMsg, e);
			}
			return null;
			// return new Message<>(MessageType.SHUTDOWN, null);
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
	public void onClose(Session session) {
		LOG.info("Closed Websocket connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		Task<?> currentTask = masterEndpoint.getCurrentTask();
		if (currentTask != null) {
			try {
				taskEndpoint.reschedule(currentTask.getTaskId());
			} catch (TaskException | ShutdownException e) {
				LOG.error("Error while closing WebSocket", e);
			}
		}
		close = true;
		ShutdownWaitingService.unregister();
	}

	@OnError
	public void onError(Session session, Throwable t) {
		Task<?> currentTask = masterEndpoint.getCurrentTask();
		LOG.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		try {
			taskEndpoint.reschedule(currentTask.getTaskId());
		} catch (TaskException | ShutdownException e) {
			LOG.error("Error while handling WebSocket error", e);
		}
		ShutdownWaitingService.unregister();
	}

	private void buildMasterEndpoint(String path) {
		masterEndpoint = new DefaultMasterImpl<>(path);
		taskEndpoint = new TaskEndpointImpl<>(path);
	}

}
