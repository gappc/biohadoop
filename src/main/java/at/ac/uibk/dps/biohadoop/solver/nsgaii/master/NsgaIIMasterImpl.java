package at.ac.uibk.dps.biohadoop.solver.nsgaii.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;

public class NsgaIIMasterImpl implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(NsgaIIMasterImpl.class);

	private final Endpoint endpoint;
	private final TaskEndpoint<double[], double[]> taskEndpoint = new TaskEndpointImpl<>(
			NsgaII.NSGAII_QUEUE);
	private Task<double[]> currentTask = null;

	public NsgaIIMasterImpl(Endpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void handleRegistration() {
		endpoint.receive();
		LOG.info("Got registration request");
		Message<Double[][]> message = new Message<>(
				MessageType.REGISTRATION_RESPONSE, null);
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
		Message<double[]> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Message<double[]> message = null;
		Task<double[]> result = incomingMessage.getPayload();
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