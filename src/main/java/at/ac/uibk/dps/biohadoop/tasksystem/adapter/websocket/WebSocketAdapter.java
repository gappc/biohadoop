package at.ac.uibk.dps.biohadoop.tasksystem.adapter.websocket;

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

import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.HandleMessageException;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.TaskConsumer;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.webserver.handler.DeployingClasses;

@ServerEndpoint(value = "/{path}", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketAdapter<R, T, S> implements Adapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketAdapter.class);

	private TaskConsumer<R, T, S> taskConsumer;
	private boolean close = false;

	@Override
	public void configure(String settingName) {
		DeployingClasses.addWebSocketClass(WebSocketAdapter.class);
	}

	@Override
	public void start() throws AdapterException {
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
		ShutdownWaitingService.register();

		session.getRequestURI();
		taskConsumer = new TaskConsumer<>(path);
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
		}

		try {
			return taskConsumer.handleMessage(inputMessage);
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
		Task<?> currentTask = taskConsumer.getCurrentTask();
		if (currentTask != null) {
			try {
				taskConsumer.reschedule(currentTask.getTaskId());
			} catch (ShutdownException e) {
				LOG.error(
						"Error while rescheduling task {}, got ShutdownException",
						currentTask.getTaskId(), e);
			}
		}
		close = true;
		ShutdownWaitingService.unregister();
	}

	@OnError
	public void onError(Session session, Throwable t) {
		Task<?> currentTask = taskConsumer.getCurrentTask();
		LOG.error(
				"Websocket error for URI {} and sessionId {}, affected task: {} ",
				session.getRequestURI(), session.getId(), currentTask, t);
		try {
			taskConsumer.reschedule(currentTask.getTaskId());
		} catch (ShutdownException e) {
			LOG.error(
					"Error while handling WebSocket error, could not reschedule task {}, got ShutdownException",
					currentTask.getTaskId(), e);
		}
		ShutdownWaitingService.unregister();
	}

}