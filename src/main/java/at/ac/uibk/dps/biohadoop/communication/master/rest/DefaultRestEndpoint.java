package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.HandleMessageException;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskException;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;
import at.ac.uibk.dps.biohadoop.webserver.handler.DeployingClasses;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
@Dependent
public class DefaultRestEndpoint<R, T, S> implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultRestEndpoint.class);
	private static final Map<String, DefaultMasterImpl<?, ?, ?>> MASTERS = new ConcurrentHashMap<>();

	public void configure(String queueName) {
		DefaultMasterImpl<?, ?, ?> master = new DefaultMasterImpl<>(queueName);
		MASTERS.put(queueName, master);
		DeployingClasses.addRestfulClass(DefaultRestEndpoint.class);
	}

	public void start() {
	}

	public void stop() {
	}

	@GET
	@Path("{path}/initialdata/{className}/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> getInitialData(@PathParam("path") String path,
			@PathParam("className") String className,
			@PathParam("taskId") String taskIdString) {
		TaskId taskId = TaskId.newInstance(taskIdString);
		Message<S> inputMessage = new Message<>(
				MessageType.REGISTRATION_REQUEST, new ClassNameWrappedTask<S>(
						taskId, null, className));
		try {
			DefaultMasterImpl<R, T, S> masterEndpoint = getMasterEndpoint(path);
			return masterEndpoint.handleMessage(inputMessage);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", inputMessage, e);
		}
		return null;
	}

	@GET
	@Path("{path}/workinit")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> workinit(@PathParam("path") String path) {
		DefaultMasterImpl<R, T, S> masterEndpoint = getMasterEndpoint(path);
		if (masterEndpoint == null) {
			return new Message<>(MessageType.ERROR, null);
		}
		Message<S> inputMessage = new Message<>(MessageType.WORK_INIT_REQUEST,
				null);
		try {
			return masterEndpoint.handleMessage(inputMessage);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", inputMessage, e);
		}
		return null;
	}

	@POST
	@Path("{path}/work")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> work(@PathParam("path") String path, String message) {
		DefaultMasterImpl<R, T, S> masterEndpoint = getMasterEndpoint(path);
		try {
			Message<S> inputMessage = MessageConverter
					.getTypedMessageForMethod(message, "compute", -1);
			return masterEndpoint.handleMessage(inputMessage);
		} catch (ConversionException e) {
			LOG.error("Error while converting String to Message", e);
			tryReschedule(path, message);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", message, e);
			tryReschedule(path, message);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private DefaultMasterImpl<R, T, S> getMasterEndpoint(String path) {
		return (DefaultMasterImpl<R, T, S>) MASTERS.get(path);
	}

	private void tryReschedule(String queueName, String message) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Message<?> rescheduleMessage = objectMapper.readValue(message,
					Message.class);

			TaskEndpoint<?, ?, ?> taskEndpoint = new TaskEndpointImpl<>(
					queueName);
			taskEndpoint.reschedule(rescheduleMessage.getTask().getTaskId());
		} catch (IOException e) {
			LOG.error("Could not reschedule task", e);
		} catch (TaskException | ShutdownException e) {
			LOG.error("Error while rescheduling task", e);
		}
	}

}
