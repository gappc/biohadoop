package at.ac.uibk.dps.biohadoop.connection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class MasterEndpointImpl<T, S> implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(MasterEndpointImpl.class);

	private final Endpoint endpoint;
	private final Object registrationObject;
	private final TaskEndpoint<T, S> taskEndpoint;
	private Task<T> currentTask = null;

	public static MasterEndpoint newInstance(Endpoint endpoint, String queueName, Object registrationObject) {
		try {
			Constructor<MasterEndpointImpl> constructor = MasterEndpointImpl.class.getDeclaredConstructor(Endpoint.class, String.class, Object.class);
			return constructor.newInstance(endpoint, queueName, registrationObject);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.error("Could not instanciate new MasterEndpointImpl", e);
			throw new InstantiationError("Could not instanciate new MasterEndpointImpl");
		}
	}
	
	public MasterEndpointImpl(Endpoint endpoint, String queueName, Object registrationObject) {
		this.endpoint = endpoint;
		this.registrationObject = registrationObject;
		taskEndpoint = new TaskEndpointImpl<>(queueName);
	}
	
	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public void handleRegistration() {
		endpoint.receive();
		LOG.info("Got registration request");
		Task<?> task = new Task<>(TaskId.newInstance(), registrationObject);
		Message<?> message = new Message<>(MessageType.REGISTRATION_RESPONSE,
				task);
		endpoint.send(message);
	}

	@Override
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

	@Override
	public void handleWork() {
		Message<S> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Message<?> message = null;
		Task<S> result = incomingMessage.getPayload();
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

	@Override
	public Task<?> getCurrentTask() {
		return currentTask;
	}
}
