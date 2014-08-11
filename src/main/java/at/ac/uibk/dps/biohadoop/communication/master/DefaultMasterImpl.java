package at.ac.uibk.dps.biohadoop.communication.master;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class DefaultMasterImpl {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultMasterImpl.class);

//	private final MasterSendReceive endpoint;
//	private final Object registrationObject;
	private final TaskEndpoint<Object, Object> taskEndpoint;
	private Task<?> currentTask = null;

	private DefaultMasterImpl(String queueName) {
//		this.endpoint = endpoint;
//		this.registrationObject = registrationObject;
		taskEndpoint = new TaskEndpointImpl<>(queueName);
	}

	public static DefaultMasterImpl newInstance(String queueName) {
		try {
			Constructor<DefaultMasterImpl> constructor = DefaultMasterImpl.class
					.getDeclaredConstructor(String.class);
			return constructor.newInstance(queueName);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			String errMsg = "Could not instanciate new "
					+ DefaultMasterImpl.class.getCanonicalName();
			LOG.error(errMsg, e);
			throw new InstantiationError(errMsg);
		}
	}

	public Message<?> handleRegistration(Object registrationObject) {
		LOG.info("Got registration request");
		Task<?> task = new Task<>(TaskId.newInstance(), registrationObject);
		Message<?> message = new Message<>(MessageType.REGISTRATION_RESPONSE,
				task);
		currentTask = null;
		return message;
	}

	public Message<?> handleWorkInit() {
		LOG.debug("Got work init request");
		Message<?> message = null;
		try {
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_INIT_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, stopping work");
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		return message;
	}

	public Message<?> handleWork(Message<?> incomingMessage) {
		LOG.debug("Got work request");

		Message<?> message = null;
		Task<?> result = incomingMessage.getTask();
		try {
			taskEndpoint.putResult(result.getTaskId(), result.getData());
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, stopping work");
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		return (message);
	}
	
//	public void handleRegistration() throws CommunicationException {
//		receive();
//		LOG.info("Got registration request");
//		Task<?> task = new Task<>(TaskId.newInstance(), registrationObject);
//		Message<?> message = new Message<>(MessageType.REGISTRATION_RESPONSE,
//				task);
//		currentTask = null;
//		send(message);
//	}
//
//	public void handleWorkInit() throws CommunicationException {
//		receive();
//		LOG.debug("Got work init request");
//		Message<?> message = null;
//		try {
//			currentTask = taskEndpoint.getTask();
//			message = new Message<>(MessageType.WORK_INIT_RESPONSE, currentTask);
//		} catch (InterruptedException e) {
//			LOG.debug("Got InterruptedException, stopping work");
//			currentTask = null;
//			message = new Message<>(MessageType.SHUTDOWN, null);
//		}
//		send(message);
//	}
//
//	public void handleWork() throws CommunicationException {
//		Message<?> incomingMessage = receive();
//		LOG.debug("Got work request");
//
//		Message<?> message = null;
//		Task<?> result = incomingMessage.getPayload();
//		try {
//			taskEndpoint.putResult(result.getTaskId(), result.getData());
//			currentTask = taskEndpoint.getTask();
//			message = new Message<>(MessageType.WORK_RESPONSE, currentTask);
//		} catch (InterruptedException e) {
//			LOG.debug("Got InterruptedException, stopping work");
//			currentTask = null;
//			message = new Message<>(MessageType.SHUTDOWN, null);
//		}
//		send(message);
//	}

	@SuppressWarnings("unchecked")
	public <T> Task<T> getCurrentTask() {
		return (Task<T>) currentTask;
	}

//	private <T> Message<T> receive() throws CommunicationException {
//		try {
//			return endpoint.receive();
//		} catch (ReceiveException e) {
//			LOG.error("Could not recevie message", e);
//			throw new CommunicationException(e);
//		}
//	}
//
//	private void send(Message<?> message) throws CommunicationException {
//		try {
//			endpoint.send(message);
//		} catch (SendException e) {
//			LOG.error("Could not send message {}", message, e);
//			if (currentTask != null) {
//				try {
//					taskEndpoint.reschedule(currentTask.getTaskId());
//				} catch (InterruptedException e1) {
//					LOG.error("Could not reschedule task at {}", currentTask, e);
//				}
//			}
//			throw new CommunicationException(e);
//		}
//	}
}
