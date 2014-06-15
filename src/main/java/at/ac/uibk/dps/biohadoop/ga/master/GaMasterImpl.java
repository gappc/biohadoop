package at.ac.uibk.dps.biohadoop.ga.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;

public class GaMasterImpl implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaMasterImpl.class);

	private final Endpoint endpoint;
	private final JobManager<?, ?> jobManager = JobManager.getInstance();
	private Task<?> currentTask = null;

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
		JobManager<?, ?> workInitManager = JobManager.getInstance();
		currentTask = workInitManager.getTask(Ga.GA_QUEUE);
		MessageType messageType = null;
		if (currentTask == null) {
			messageType = MessageType.SHUTDOWN;
		} else {
			messageType = MessageType.WORK_INIT_RESPONSE;
		}
		Message<?> message = new Message<>(messageType, currentTask);
		endpoint.send(message);
//		if (messageType == MessageType.SHUTDOWN) {
//			throw new ShutdownException();
//		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void handleWork() {
		Message<?> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Task result = incomingMessage.getPayload();
		jobManager.putResult(result, Ga.GA_QUEUE);

		MessageType messageType = null;
		currentTask = jobManager.getTask(Ga.GA_QUEUE);
		if (currentTask == null) {
			messageType = MessageType.SHUTDOWN;
		} else {
			messageType = MessageType.WORK_RESPONSE;
		}
		Message<?> message = new Message<>(messageType, currentTask);
		endpoint.send(message);
//		if (messageType == MessageType.SHUTDOWN) {
//			throw new ShutdownException();
//		}
	}

	@Override
	public Task<?> getCurrentTask() {
		return currentTask;
	}

}
