package at.ac.uibk.dps.biohadoop.connection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class DefaultEndpointHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultEndpointHandler.class);

	private final Endpoint endpoint;
	private final Object registrationObject;
	private final TaskEndpoint<Object, Object> taskEndpoint;
	private Task<?> currentTask = null;

	public static DefaultEndpointHandler newInstance(Endpoint endpoint,
			String queueName, Object registrationObject) {
		try {
			Constructor<DefaultEndpointHandler> constructor = DefaultEndpointHandler.class
					.getDeclaredConstructor(Endpoint.class, String.class,
							Object.class);
			return constructor.newInstance(endpoint, queueName,
					registrationObject);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error("Could not instanciate new MasterEndpointImpl", e);
			throw new InstantiationError(
					"Could not instanciate new MasterEndpointImpl");
		}
	}

	private DefaultEndpointHandler(Endpoint endpoint, String queueName,
			Object registrationObject) {
		this.endpoint = endpoint;
		this.registrationObject = registrationObject;
		taskEndpoint = new TaskEndpointImpl<>(queueName);
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void handleRegistration() {
		endpoint.receive();
		LOG.info("Got registration request");
		Task<?> task = new Task<>(TaskId.newInstance(), registrationObject);
		Message<?> message = new Message<>(MessageType.REGISTRATION_RESPONSE,
				task);
		endpoint.send(message);
	}

	public void handleWorkInit() {
		endpoint.receive();
		LOG.debug("Got work init request");
		Message<?> message = null;
		try {
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_INIT_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		endpoint.send(message);
	}

	public void handleWork() {
		Message<?> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Message<?> message = null;
		Task<?> result = incomingMessage.getPayload();
		try {
			taskEndpoint.putResult(result.getTaskId(), result.getData());
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		endpoint.send(message);
	}

	public Task<?> getCurrentTask() {
		return currentTask;
	}
}