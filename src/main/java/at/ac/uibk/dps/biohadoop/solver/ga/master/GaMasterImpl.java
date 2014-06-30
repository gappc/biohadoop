package at.ac.uibk.dps.biohadoop.solver.ga.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

public class GaMasterImpl implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaMasterImpl.class);

	private final Endpoint endpoint;
	private final TaskEndpoint<int[], Double> taskEndpoint = new TaskEndpointImpl<>(
			Ga.GA_QUEUE);
	private Task<int[]> currentTask = null;

	public GaMasterImpl(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public void handleRegistration() {
		endpoint.receive();
		LOG.info("Got registration request");
		Double[][] distances = DistancesGlobal.getDistancesAsObject();
		Task<Double[][]> task = new Task<Double[][]>(TaskId.newInstance(),
				distances);
		Message<Double[][]> message = new Message<>(
				MessageType.REGISTRATION_RESPONSE, task);
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
		Message<Double> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Message<?> message = null;
		Task<Double> result = incomingMessage.getPayload();
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
