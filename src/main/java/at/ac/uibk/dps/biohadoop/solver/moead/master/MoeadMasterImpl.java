package at.ac.uibk.dps.biohadoop.solver.moead.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.service.job.Task;
import at.ac.uibk.dps.biohadoop.service.job.api.JobService;
import at.ac.uibk.dps.biohadoop.service.job.remote.Message;
import at.ac.uibk.dps.biohadoop.service.job.remote.MessageType;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

public class MoeadMasterImpl implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(MoeadMasterImpl.class);

	private final Endpoint endpoint;
	private final JobService<?, ?> jobService = JobService.getInstance();
	private Task<?> currentTask = null;

	public MoeadMasterImpl(Endpoint endpoint) {
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
		JobService<?, ?> workInitManager = JobService.getInstance();
		currentTask = workInitManager.getTask(Moead.MOEAD_QUEUE);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleWork() {
		Message<?> incomingMessage = endpoint.receive();
		LOG.debug("Got work request");

		Task result = incomingMessage.getPayload();
		jobService.putResult(result, Moead.MOEAD_QUEUE);

		MessageType messageType = null;
		currentTask = jobService.getTask(Moead.MOEAD_QUEUE);
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
